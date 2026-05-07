package com.flashsale.booking.domain.booking.dto;

import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BookingPaymentRequest {
    private Long userId;
    private Long accommodationId;

    // 복합 결제 수단 리스트 (기존에 만든 PaymentDetail 재사용)
    private List<PaymentRequest.PaymentDetail> payMethods;
}