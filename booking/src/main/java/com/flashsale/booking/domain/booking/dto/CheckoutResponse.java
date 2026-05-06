package com.flashsale.booking.domain.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckoutResponse {
    private Long accommodationId;
    private String accommodationName;
    private int totalAmount;
    private Long userId;
    private int pointBalance;
}