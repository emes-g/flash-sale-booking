package com.flashsale.booking.domain.booking.repository;

import com.flashsale.booking.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Fallback 멱등성 검증용: 특정 시간(예: 5초 전) 이후에 해당 유저가 같은 숙소를 예약한 이력이 있는지 확인
    boolean existsByUserIdAndAccommodationIdAndCreatedAtAfter(Long userId, Long accommodationId, LocalDateTime time);
}