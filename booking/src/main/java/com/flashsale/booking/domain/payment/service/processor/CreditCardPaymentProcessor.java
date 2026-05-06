package com.flashsale.booking.domain.payment.service.processor;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.user.entity.User;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CreditCardPaymentProcessor implements PaymentProcessor {

    @Override
    public boolean supports(String paymentMethod) {
        return "CREDIT_CARD".equals(paymentMethod);
    }

    @Override
    public Payment process(Booking booking, User user, String provider, int amount) {
        // 외부 PG사 API 호출 생략 (Mocking)
        String mockTransactionId = "PG-" + UUID.randomUUID();

        return Payment.builder()
                .booking(booking)
                .paymentType("EXTERNAL")
                .paymentMethod("CREDIT_CARD")
                .provider(provider)
                .providerTransactionId(mockTransactionId)
                .amount(amount)
                .status("SUCCESS")
                .build();
    }
}