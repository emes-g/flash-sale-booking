package com.flashsale.booking.domain.payment.service;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.domain.booking.repository.BookingRepository;
import com.flashsale.booking.domain.payment.dto.PaymentRequest;
import com.flashsale.booking.domain.payment.entity.Payment;
import com.flashsale.booking.domain.payment.repository.PaymentRepository;
import com.flashsale.booking.domain.payment.service.processor.PaymentProcessor;
import com.flashsale.booking.domain.user.entity.User;
import com.flashsale.booking.domain.user.repository.UserRepository;
import com.flashsale.booking.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final List<PaymentProcessor> paymentProcessors; // 모든 결제 처리기들이 자동으로 주입

    @Transactional
    public Long processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 예약입니다."));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BusinessException("결제 대기 상태의 예약이 아닙니다.");
        }

        // 1. 외부 결제 수단 중복 체크
        long externalPayCount = request.getPayMethods().stream()
                .filter(method -> !"Y_POINT".equals(method.getPaymentMethod()))
                .count();
        if (externalPayCount > 1) {
            throw new BusinessException("외부 결제 수단은 하나만 사용할 수 있습니다.");
        }

        // 2. 총 결제 금액 검증
        int totalPayAmount = request.getPayMethods().stream()
                .mapToInt(PaymentRequest.PaymentDetail::getAmount)
                .sum();
        if (booking.getTotalAmount() != totalPayAmount) {
            throw new BusinessException("결제 요청 금액이 총 예약 금액과 일치하지 않습니다.");
        }

        // 3. 결제 처리 및 저장
        for (PaymentRequest.PaymentDetail detail : request.getPayMethods()) {
            PaymentProcessor processor = paymentProcessors.stream()
                    .filter(p -> p.supports(detail.getPaymentMethod()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("지원하지 않는 결제 수단입니다: " + detail.getPaymentMethod()));

            Payment payment = processor.process(booking, user, detail.getProvider(), detail.getAmount());
            paymentRepository.save(payment);
        }

        // 4. 예약 확정
        booking.complete();

        return booking.getId();
    }
}