package com.flashsale.booking.domain.payment.service.processor;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class YPointPaymentProcessor implements PaymentProcessor {

    @Override
    public boolean supports(String paymentMethod) {
        return "Y_POINT".equals(paymentMethod);
    }

    @Override
    public Payment process(Booking booking, User user, String provider, int amount) {
        // User 엔티티의 포인트 차감 비즈니스 로직 호출
        user.deductPoint(amount);

        return Payment.builder()
                .booking(booking)
                .paymentType("INTERNAL")
                .paymentMethod("Y_POINT")
                .provider(provider != null ? provider : "SYSTEM")
                .amount(amount)
                .status("SUCCESS")
                .build();
    }
}