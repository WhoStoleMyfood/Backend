package com.example.whostolemyfood.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private UUID menuId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer priceAtOrder; // 주문 시점의 가격

    // [Audit] 필수 기능 명세에 따른 기록 전용 필드 (수정/삭제 미발생 도메인 특성 반영)
    // 명세서 지침에 따라 BaseEntity 상속 없이 생성 정보만 직접 관리함
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private UUID createdBy; // TODO: [인증/인가] 주문자 ID 연동

    // 필수 기능 명세에 따라 데이터 저장 전 생성일(createdAt) 자동 기록
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); 
    }
}
