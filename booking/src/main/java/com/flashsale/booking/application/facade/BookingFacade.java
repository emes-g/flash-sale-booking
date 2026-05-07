package com.flashsale.booking.application.facade;

import com.flashsale.booking.domain.booking.dto.BookingPaymentRequest;
import com.flashsale.booking.domain.booking.dto.BookingRequest;
import com.flashsale.booking.domain.booking.service.BookingService;
import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import com.flashsale.booking.domain.payment.service.PaymentService;
import com.flashsale.booking.global.annotation.DistributedLock;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final RedissonClient redissonClient;

    /**
     * 예약과 결제를 하나의 비즈니스 흐름으로 통합하여 제어한다.
     * 1. (정합성) @DistributedLock을 통해 대규모 트래픽 하에서도 안전한 재고 차감 보장
     * 2. (가용성) 외부 PG API 호출 지연에 따른 DB 커넥션 풀 고갈을 방지하기 위해,
     *    거대한 단일 @Transactional 대신 트랜잭션을 분리하고 실패 시 보상 트랜잭션(Saga) 수행
     */
    @DistributedLock(key = "'ACCOMMODATION:' + #request.accommodationId")
    public Long checkoutAndPay(BookingPaymentRequest request) {

        // 1. 멱등성 검증 (단기간 내 연속된 중복 결제 요청 방지)
        String idempotentKey = "IDEMPOTENT:" + request.getUserId() + ":" + request.getAccommodationId();
        RBucket<String> bucket = redissonClient.getBucket(idempotentKey);

        // 5초 동안만 유지되는 키를 생성. 만약 이미 키가 존재하면(false 반환) 중복 요청으로 간주하여 차단
        if (!bucket.trySet("PROCESSING", 5, TimeUnit.SECONDS)) {
            throw new BusinessException("이미 처리 중인 결제 요청입니다.");
        }

        Long bookingId = null;
        try {

            // 2. 예약 트랜잭션: 재고 차감 및 PENDING 상태 예약 생성
            BookingRequest bookingRequest = BookingRequest.builder().
                    userId(request.getUserId()).
                    accommodationId(request.getAccommodationId()).
                    build();

            bookingId = bookingService.createBooking(bookingRequest);

            // 3. 결제 트랜잭션: 외부 PG 연동 및 COMPLETED 상태 변경
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(bookingId)
                    .userId(request.getUserId())
                    .payMethods(request.getPayMethods())
                    .build();

            paymentService.processPayment(paymentRequest);

            return bookingId;

        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생. 예약을 롤백합니다. 락 키: {}", request.getAccommodationId(), e);

            // 4. 실패 시 보상 트랜잭션 (Saga Pattern): 예약 취소 및 재고 복구
            if (bookingId != null) {
                bookingService.rollbackBooking(bookingId);
            }
            throw e; // 예외를 다시 던져서 사용자에게 실패를 알림

        } finally {
            // 처리가 끝났어도, 5초 전에 다른 요청을 받을 수 있도록 멱등성 키 즉시 삭제
            bucket.delete();
        }
    }

    /**
     * Redis 장애 발생 시 호출되는 Fallback 메서드.
     * @DistributedLock 적용 없이 내부적으로 DB 비관적 락이 적용된 메서드를 호출한다.
     */
    public Long checkoutAndPayFallback(BookingPaymentRequest request) {
        log.warn("Redis 장애 감지. DB 비관적 락(Fallback)으로 결제를 진행합니다. 숙소 ID: {}", request.getAccommodationId());

        // 1. DB 기반 멱등성 검증 (단기간 내 연속된 중복 결제 요청 방지)
        bookingService.validateDuplicateRequest(request.getUserId(), request.getAccommodationId());

        Long bookingId = null;
        try {
            BookingRequest bookingRequest = BookingRequest.builder()
                    .userId(request.getUserId())
                    .accommodationId(request.getAccommodationId())
                    .build();

            // 2. 예약 트랜잭션: 비관적 락 기반으로 재고 차감 및 PENDING 예약 생성
            bookingId = bookingService.createBookingWithPessimisticLock(bookingRequest);

            // 3. 결제 트랜잭션: 앞서 수정한 안전한 구조의 processPayment 호출
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(bookingId)
                    .userId(request.getUserId())
                    .payMethods(request.getPayMethods())
                    .build();

            paymentService.processPayment(paymentRequest);

            return bookingId;

        } catch (Exception e) {
            log.error("Fallback 결제 처리 중 오류 발생. 예약을 롤백합니다. 숙소 ID: {}", request.getAccommodationId(), e);
            if (bookingId != null) {
                bookingService.rollbackBooking(bookingId);
            }
            throw e;
        }
    }
}