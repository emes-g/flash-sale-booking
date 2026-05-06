package com.flashsale.booking.domain.booking.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long accommodationId;
}