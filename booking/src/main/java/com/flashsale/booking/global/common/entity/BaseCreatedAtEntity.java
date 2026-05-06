package com.flashsale.booking.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass   // 해당 클래스를 상속받는 자식 클래스의 테이블에 부모가 가진 필드(컬럼) 정보 추가
@EntityListeners(AuditingEntityListener.class)  // JPA의 시간 주입 대상으로 지정
public abstract class BaseCreatedAtEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}