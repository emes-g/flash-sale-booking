package com.flashsale.booking.domain.user.entity;

import com.flashsale.booking.global.common.entity.BaseCreatedAtEntity;
import com.flashsale.booking.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseCreatedAtEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int pointBalance;

    // 포인트 차감
    public void deductPoint(int amount) {
        if (this.pointBalance < amount) {
            throw new BusinessException("포인트 잔액이 부족합니다.");
        }
        this.pointBalance -= amount;
    }
}