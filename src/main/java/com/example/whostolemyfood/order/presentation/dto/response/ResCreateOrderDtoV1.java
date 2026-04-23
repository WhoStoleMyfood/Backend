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
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String message; // 메시지 필드 추가

    // 서비스에서 메시지를 함께 보낼 수 있도록 수정
    public static ResCreateOrderDtoV1 from(OrderEntity order, String message) {
        return ResCreateOrderDtoV1.builder()
                .orderId(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .message(message)
                .build();
    }
    
    // 기존 호출(메시지 없는 경우) 호환성 유지
    public static ResCreateOrderDtoV1 from(OrderEntity order) {
        return from(order, null);
    }
}
