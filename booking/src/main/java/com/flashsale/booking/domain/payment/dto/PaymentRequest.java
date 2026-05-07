package com.flashsale.booking.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PaymentRequest {
    private Long bookingId;
    private Long userId;
    private List<PaymentDetail> payMethods; // 복합 결제 리스트

    @Getter
    @NoArgsConstructor
    public static class PaymentDetail {
        private String paymentMethod; // Y_POINT, CREDIT_CARD 등
        private String provider; // SYSTEM, Y 등
        private int amount;
    }

    @Builder
    public PaymentRequest(Long bookingId, Long userId, List<PaymentDetail> payMethods) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.payMethods = payMethods;
    }
}