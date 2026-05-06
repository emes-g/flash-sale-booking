package com.flashsale.booking.domain.payment.entity;

import com.flashsale.booking.domain.booking.entity.Booking;
import com.flashsale.booking.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // 결제 성격: INTERNAL(내부 DB 트랜잭션), EXTERNAL(외부 PG 연동)
    private String paymentType;

    // 결제 수단: CREDIT_CARD, Y_PAY, Y_POINT 등
    private String paymentMethod;

    // 결제 처리 주체: SYSTEM(내부), TOSS, KAKAO 등
    private String provider;

    // PG사 고유 승인 번호 또는 내부 포인트 차감 트랜잭션 ID
    private String providerTransactionId;

    private int amount;

    // 결제 상태: SUCCESS(성공), FAILED(실패), REFUNDED(환불)
    private String status;

    // 결제 수단별 세부 정보 (예: 카드사, 할부 개월 수, 연동 계좌 등)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> paymentDetails;
}