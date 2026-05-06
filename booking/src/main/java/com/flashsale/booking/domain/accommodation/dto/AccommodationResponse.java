package com.flashsale.booking.domain.accommodation.dto;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AccommodationResponse {
    private Long id;
    private String name;
    private int price;
    private LocalDateTime openAt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static AccommodationResponse from(Accommodation accommodation) {
        return new AccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getPrice(),
                accommodation.getOpenAt(),
                accommodation.getCheckInTime(),
                accommodation.getCheckOutTime()
        );
    }
}