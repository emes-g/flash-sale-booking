package com.flashsale.booking.domain.accommodation.entity;

import com.flashsale.booking.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AccommodationStock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    private int stock;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void decrease() {
        if (this.stock <= 0) {
            throw new BusinessException("재고가 모두 소진되었습니다.");
        }
        this.stock--;
    }

    // 결제 실패 시 재고 복구 (Rollback)
    public void increase() {
        this.stock++;
    }
}