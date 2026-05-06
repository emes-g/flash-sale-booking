package com.flashsale.booking.domain.booking.entity;

import com.flashsale.booking.domain.accommodation.entity.Accommodation;
import com.flashsale.booking.domain.user.entity.User;
import com.flashsale.booking.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    // 예약 상태: PENDING(결제 대기), COMPLETED(예약 확정), CANCELED(취소됨), FAILED(실패)
    private String status;

    private int totalAmount;

    @Builder
    public Booking(User user, Accommodation accommodation, String status, int totalAmount) {
        this.user = user;
        this.accommodation = accommodation;
        this.status = status;
        this.totalAmount = totalAmount;
    }
}