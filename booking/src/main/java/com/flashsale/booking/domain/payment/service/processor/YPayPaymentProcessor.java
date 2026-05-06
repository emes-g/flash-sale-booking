package com.flashsale.booking.domain.payment.service.processor;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.user.entity.User;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class YPayPaymentProcessor implements PaymentProcessor {

    @Override
    public boolean supports(String paymentMethod) {
        return "Y_PAY".equals(paymentMethod);
    }

    @Override
    public Payment process(Booking booking, User user, String provider, int amount) {
        // Y페이 외부 PG 연동 Mocking
        String mockTransactionId = "YPAY-" + UUID.randomUUID().toString();

        return Payment.builder()
                .booking(booking)
                .paymentType("EXTERNAL")
                .paymentMethod("Y_PAY")
                .provider(provider != null ? provider : "Y")    // Y페이이므로 디폴트는 일단 Y로 설정
                .providerTransactionId(mockTransactionId)
                .amount(amount)
                .status("SUCCESS")
                .build();
    }
}