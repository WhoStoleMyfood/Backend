package com.example.whostolemyfood.order.presentation.dto.response;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResCreateOrderDtoV1 {
    private UUID orderId;
    private Integer totalPrice;
    private Integer deliveryFee;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String message;

    public static ResCreateOrderDtoV1 from(OrderEntity order) {
        return ResCreateOrderDtoV1.builder()
                .orderId(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .message("주문이 성공적으로 생성되었습니다.")
                .build();
    }
}
