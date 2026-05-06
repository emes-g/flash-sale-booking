package com.flashsale.booking.domain.payment.service.processor;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.user.entity.User;

public interface PaymentProcessor {

    // 이 처리기가 해당 결제 수단을 지원하는지 (HandlerMapping의 supports()와 유사)
    boolean supports(String paymentMethod);

    // 해당 결제 수단을 지원하는 경우, 결제 로직 처리(진행)
    Payment process(Booking booking, User user, String provider, int amount);
}