package com.flashsale.booking.domain.payment.service;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.booking.repository.BookingRepository;
import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.payment.repository.PaymentRepository;
import com.flashsale.booking.domain.payment.service.processor.PaymentProcessor;
import com.flashsale.booking.domain.user.entity.User;
import com.flashsale.booking.domain.user.repository.UserRepository;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final List<PaymentProcessor> paymentProcessors; // 모든 결제 처리기들이 자동으로 주입

    // 메서드 전체가 아니라 필요한 곳에만 트랜잭션을 걸기 위해 TransactionTemplate 사용
    private final TransactionTemplate transactionTemplate;

    // 메서드 레벨의 @Transactional 어노테이션 제거
    public void processPayment(PaymentRequest request) {

        // 1. 엔티티 조회 및 검증 (읽기 전용이므로 트랜잭션 없이 진행 가능)
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 예약입니다."));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException("결제 대기 상태의 예약이 아닙니다.");
        }

        // 1-1. 외부 결제 수단 중복 체크
        long externalPayCount = request.getPayMethods().stream()
                .filter(method -> !"Y_POINT".equals(method.getPaymentMethod()))
                .count();
        if (externalPayCount > 1) {
            throw new BusinessException("외부 결제 수단은 하나만 사용할 수 있습니다.");
        }

        // 1-2. 총 결제 금액 검증
        int totalPayAmount = request.getPayMethods().stream()
                .mapToInt(PaymentRequest.PaymentDetail::getAmount)
                .sum();
        if (booking.getTotalAmount() != totalPayAmount) {
            throw new BusinessException("결제 요청 금액이 총 예약 금액과 일치하지 않습니다.");
        }

        // 1-3. 지원하는 결제 수단인지 사전 검증 (Fail-Fast)
        // 실제 결제 처리가 진행되기 전에 미리 예외를 발생시켜 일부 상태만 변경되는 것을 방지한다.
        for (PaymentRequest.PaymentDetail detail : request.getPayMethods()) {
            boolean isSupported = paymentProcessors.stream()
                    .anyMatch(p -> p.supports(detail.getPaymentMethod()));
            if (!isSupported) {
                throw new BusinessException("지원하지 않는 결제 수단입니다: " + detail.getPaymentMethod());
            }
        }

        // 2. 결제 처리 로직 실행 (외부 API 통신 포함)
        // 트랜잭션 외부에서 실행되므로, 이 과정이 지연되어도 DB 커넥션을 점유하지 않는다.
        List<Payment> processedPayments = new ArrayList<>();
        for (PaymentRequest.PaymentDetail detail : request.getPayMethods()) {
            PaymentProcessor processor = paymentProcessors.stream()
                    .filter(p -> p.supports(detail.getPaymentMethod()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("사전 검증 로직 누락 발생"));

            Payment payment = processor.process(booking, user, detail.getProvider(), detail.getAmount());
            processedPayments.add(payment);
        }

        // 3. DB 쓰기 작업 (짧은 트랜잭션 생성)
        // 외부 통신이 모두 끝난 후, 데이터베이스 상태를 변경하는 작업만 트랜잭션으로 묶는다.
        transactionTemplate.executeWithoutResult(status -> {
            // 결제 내역 저장
            paymentRepository.saveAll(processedPayments);

            // 예약 상태 확정
            booking.complete();

            // 트랜잭션 범위 밖에서 변경된 엔티티 객체의 상태를 DB에 반영하기 위해 명시적으로 save 호출
            // (Y_POINT 결제로 인해 user의 포인트가 차감된 경우 등을 반영)
            bookingRepository.save(booking);
            userRepository.save(user);
        });
    }
}