package com.flashsale.booking.domain.accommodation.dto;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Redis 역직렬화(JSON → 객체)를 위해 @NoArgsConstructor 등록
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationCacheDto {
    private Long id;
    private String name;
    private int price;

    public static AccommodationCacheDto from(Accommodation accommodation) {
        return new AccommodationCacheDto(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getPrice()
        );
    }
}