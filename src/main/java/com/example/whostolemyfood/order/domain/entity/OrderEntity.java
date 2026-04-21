package com.example.whostolemyfood.order.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderEntity extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId; // TODO: [인증/인가] SecurityContext 사용자 ID 연동

    @Column(nullable = false)
    private UUID storeId;

    @Column(nullable = false)
    private UUID addressId;

    @Column(columnDefinition = "TEXT")
    private String request;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private Integer deliveryFee;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isHidden = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // 데이터 저장(Persist) 전 초기 상태(PENDING) 설정
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.PENDING; 
        }
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
    }

    // [비즈니스 로직] 객체 스스로 상태를 보호하도록 엔티티 내부에 핵심 로직 구현 (도메인 주도 설계)
    public void updateRequest(String newRequest) {
        if (this.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("주문이 이미 수락되어 요청사항을 수정할 수 없습니다.");
        }
        this.request = newRequest;
    }

    public void updateStatus(OrderStatus nextStatus) {
        this.status = nextStatus;
    }

    public void markAsDeleted(UUID deletedBy) {
        this.isDeleted = true;
        super.delete(deletedBy);
    }
}
