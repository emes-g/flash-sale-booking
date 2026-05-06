package com.flashsale.booking.domain.booking.repository;

import com.flashsale.booking.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}