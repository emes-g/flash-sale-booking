package com.flashsale.booking.domain.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long accommodationId;

    @Builder
    public BookingRequest(Long userId, Long accommodationId) {
        this.userId = userId;
        this.accommodationId = accommodationId;
    }
}