package com.example.whostolemyfood.order.domain.entity;

import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j; // 추가
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j // 추가
@Entity
@Table(name = "p_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId; 

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
    private Boolean isHidden = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Builder
    public OrderEntity(UUID userId, UUID storeId, UUID addressId, String request, Integer totalPrice, OrderStatus status, Integer deliveryFee, Boolean isHidden) {
        this.userId = userId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.request = request;
        this.totalPrice = totalPrice;
        this.status = status;
        this.deliveryFee = deliveryFee;
        this.isHidden = isHidden != null ? isHidden : false;
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = OrderStatus.PENDING; 
        }
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
    }

    public void updateRequest(String newRequest) {
        if (this.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("주문이 이미 수락되어 요청사항을 수정할 수 없습니다.");
        }
        this.request = newRequest;
    }

    public void updateStatus(OrderStatus nextStatus) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("이미 최종 상태에 도달한 주문은 상태를 변경할 수 없습니다.");
        }

        boolean isValid = false;
        switch (this.status) {
            case PENDING: isValid = (nextStatus == OrderStatus.ACCEPTED); break;
            case ACCEPTED: isValid = (nextStatus == OrderStatus.COOKING); break;
            case COOKING: isValid = (nextStatus == OrderStatus.DELIVERING); break;
            case DELIVERING: isValid = (nextStatus == OrderStatus.DELIVERED); break;
            case DELIVERED: isValid = (nextStatus == OrderStatus.COMPLETED); break;
        }

        if (!isValid) {
            throw new IllegalStateException(this.status + " 상태에서 " + nextStatus + " 상태로의 변경은 허용되지 않습니다.");
        }

        this.status = nextStatus;
    }

    /**
     * [관리자 전용] 상태 강제 변경 (슈퍼 권한)
     */
    public void forceUpdateStatus(OrderStatus nextStatus) {
        log.info("[Order Domain] Force status update by Admin. From: {}, To: {}", this.status, nextStatus);
        this.status = nextStatus;
    }
}
