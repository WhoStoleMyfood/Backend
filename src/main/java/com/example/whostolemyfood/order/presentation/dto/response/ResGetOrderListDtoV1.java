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
public class ResGetOrderListDtoV1 {
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private UUID addressId;
    private String request;
    private Integer totalPrice;
    private Integer deliveryFee;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static ResGetOrderListDtoV1 from(OrderEntity order) {
        return ResGetOrderListDtoV1.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .addressId(order.getAddressId())
                .request(order.getRequest())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
