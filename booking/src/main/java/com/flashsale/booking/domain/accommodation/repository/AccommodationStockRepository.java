package com.flashsale.booking.domain.accommodation.repository;

import com.flashsale.booking.domain.accommodation.entity.AccommodationStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccommodationStockRepository extends JpaRepository<AccommodationStock, Long> {
    Optional<AccommodationStock> findByAccommodationId(Long accommodationId);

    // Fallback용 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AccommodationStock s WHERE s.accommodation.id = :accommodationId")
    Optional<AccommodationStock> findByAccommodationIdWithPessimisticLock(@Param("accommodationId") Long accommodationId);
}