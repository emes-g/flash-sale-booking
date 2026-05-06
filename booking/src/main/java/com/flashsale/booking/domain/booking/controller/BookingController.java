package com.flashsale.booking.domain.booking.controller;

import com.flashsale.booking.domain.booking.dto.BookingRequest;
import com.flashsale.booking.domain.booking.dto.CheckoutResponse;
import com.flashsale.booking.domain.booking.service.BookingService;
import com.flashsale.booking.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 주문서 진입
    @GetMapping("/checkout")
    public ApiResponse<CheckoutResponse> getCheckoutInfo(
            @RequestParam Long accommodationId,
            @RequestParam Long userId) {
        CheckoutResponse response = bookingService.getCheckoutInfo(accommodationId, userId);
        return ApiResponse.success(response);
    }

    // 예약
    @PostMapping
    public ApiResponse<Long> createBooking(@RequestBody BookingRequest request) {
        Long bookingId = bookingService.createBooking(request);
        return ApiResponse.success(bookingId);
    }
}