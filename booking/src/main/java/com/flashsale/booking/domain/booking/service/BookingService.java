package com.flashsale.booking.domain.booking.service;

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

    @Transactional(readOnly = true)
    public CheckoutResponse getCheckoutInfo(Long accommodationId, Long userId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new BusinessException("존재하지 않는 숙소입니다."));
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
}