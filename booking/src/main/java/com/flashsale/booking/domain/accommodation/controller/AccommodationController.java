package com.flashsale.booking.domain.accommodation.controller;

import com.flashsale.booking.domain.accommodation.dto.AccommodationResponse;
import com.flashsale.booking.domain.accommodation.service.AccommodationService;
import com.flashsale.booking.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final AccommodationService accommodationService;

    @GetMapping
    public ApiResponse<List<AccommodationResponse>> getAccommodations() {
        List<AccommodationResponse> responses = accommodationService.getAccommodations();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<AccommodationResponse> getAccommodation(@PathVariable Long id) {
        AccommodationResponse response = accommodationService.getAccommodation(id);
        return ApiResponse.success(response);
    }
}