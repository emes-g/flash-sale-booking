package com.flashsale.booking.domain.payment.repository;

import com.flashsale.booking.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}