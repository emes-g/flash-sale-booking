package com.flashsale.booking.domain.booking.service;

import com.flashsale.booking.application.service.AccommodationCacheService;
import com.flashsale.booking.domain.accommodation.dto.AccommodationCacheDto;
import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import com.flashsale.booking.domain.accommodation.entity.AccommodationStock;
import com.flashsale.booking.domain.accommodation.repository.AccommodationRepository;
import com.flashsale.booking.domain.accommodation.repository.AccommodationStockRepository;
import com.flashsale.booking.domain.booking.dto.BookingRequest;
import com.flashsale.booking.domain.booking.dto.CheckoutResponse;
import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.booking.repository.BookingRepository;
import com.flashsale.booking.domain.user.entity.User;
import com.flashsale.booking.domain.user.repository.UserRepository;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationStockRepository stockRepository;
    private final BookingRepository bookingRepository;
    private final AccommodationCacheService accommodationCacheService;

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutInfo(Long accommodationId, Long userId) {

        // 숙소 정보는 Redis Cache에서 조회
        AccommodationCacheDto accommodation = accommodationCacheService.getAccommodationInfo(accommodationId);

        // 유저 정보는 DB에서 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));

        return new CheckoutResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getPrice(),
                user.getId(),
                user.getPointBalance()
        );
    }

    @Transactional
    public Long createBooking(BookingRequest request) {
        // 1. 사용자 및 숙소 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));
        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 숙소입니다."));

        // 2. 예약 오픈 시간 검증
        if (LocalDateTime.now().isBefore(accommodation.getOpenAt())) {
            throw new BusinessException("아직 예약이 오픈되지 않았습니다.");
        }

        // 3. 재고 조회 및 차감
        AccommodationStock stock = stockRepository.findByAccommodationId(accommodation.getId())
                .orElseThrow(() -> new BusinessException("재고 정보가 존재하지 않습니다."));

        stock.decrease();

        // 4. 예약 생성 (상태: 결제 대기)
        Booking booking = Booking.builder()
                .user(user)
                .accommodation(accommodation)
                .status("PENDING")  // 추후 Enum으로 리팩토링
                .totalAmount(accommodation.getPrice())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return savedBooking.getId();
    }

    // Redis 장애 시 사용할 DB 비관적 락 기반의 예약 생성 로직
    @Transactional
    public Long createBookingWithPessimisticLock(BookingRequest request) {
        // 1. 사용자 및 숙소 조회 (일반 조회, 락 없음)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));
        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 숙소입니다."));

        // 2. 예약 오픈 시간 검증
        if (LocalDateTime.now().isBefore(accommodation.getOpenAt())) {
            throw new BusinessException("아직 예약이 오픈되지 않았습니다.");
        }

        // 3. 재고 조회 (비관적 락 적용) 및 차감
        // 다른 스레드가 락을 점유 중이라면 여기서 대기 상태(Blocking)가 됨
        AccommodationStock stock = stockRepository
                .findByAccommodationIdWithPessimisticLock(accommodation.getId())
                .orElseThrow(() -> new BusinessException("재고 정보가 존재하지 않습니다."));

        stock.decrease();

        // 4. 예약 생성 (상태: 결제 대기)
        Booking booking = Booking.builder()
                .user(user)
                .accommodation(accommodation)
                .status("PENDING")
                .totalAmount(accommodation.getPrice())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return savedBooking.getId();
    }

    // Fallback(DB) 기반 멱등성 검증
    @Transactional(readOnly = true)
    public void validateDuplicateRequest(Long userId, Long accommodationId) {
        // 현재 시간으로부터 5초 전 시간 계산
        LocalDateTime fiveSecondsAgo = LocalDateTime.now().minusSeconds(5);

        // 5초 이내에 동일한 유저가 동일한 숙소에 생성한 예약이 있다면 중복 요청으로 간주
        if (bookingRepository.existsByUserIdAndAccommodationIdAndCreatedAtAfter(userId, accommodationId, fiveSecondsAgo)) {
            throw new BusinessException("이미 처리 중이거나 최근에 완료된 결제 요청입니다.");
        }
    }

    // 보상 트랜잭션 (Saga Pattern)
    @Transactional
    public void rollbackBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("예약을 찾을 수 없습니다."));

        // 1. 예약 상태를 FAILED로 변경
        booking.fail();

        // 2. 깎았던 재고를 다시 복구 (+1)
        AccommodationStock stock = stockRepository.findByAccommodationId(booking.getAccommodation().getId())
                .orElseThrow(() -> new BusinessException("재고 정보를 찾을 수 없습니다."));
        stock.increase();
    }
}