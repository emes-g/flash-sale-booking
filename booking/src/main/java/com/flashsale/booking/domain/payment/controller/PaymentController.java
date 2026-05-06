package com.flashsale.booking.domain.payment.controller;

import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import com.flashsale.booking.domain.payment.service.PaymentService;
import com.flashsale.booking.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<Long> processPayment(@RequestBody PaymentRequest request) {
        Long completedBookingId = paymentService.processPayment(request);
        return ApiResponse.success(completedBookingId);
    }
}