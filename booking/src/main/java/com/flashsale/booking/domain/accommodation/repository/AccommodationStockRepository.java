package com.flashsale.booking.domain.accommodation.repository;

import com.flashsale.booking.domain.accommodation.entity.AccommodationStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccommodationStockRepository extends JpaRepository<AccommodationStock, Long> {
    Optional<AccommodationStock> findByAccommodationId(Long accommodationId);
}