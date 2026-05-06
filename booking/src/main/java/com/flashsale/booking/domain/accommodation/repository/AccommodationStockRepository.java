package com.flashsale.booking.domain.accommodation.repository;

import com.flashsale.booking.domain.accommodation.entity.AccommodationStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccommodationStockRepository extends JpaRepository<AccommodationStock, Long> {
}