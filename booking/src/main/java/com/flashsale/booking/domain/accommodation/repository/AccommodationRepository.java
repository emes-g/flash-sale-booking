package com.flashsale.booking.domain.accommodation.repository;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
}