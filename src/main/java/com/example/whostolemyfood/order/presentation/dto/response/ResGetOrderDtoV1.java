package com.example.whostolemyfood.order.presentation.dto.response;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResGetOrderDtoV1 {

    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private UUID addressId;
    private String request;
    private Integer totalPrice;
    private OrderStatus status;
    private Integer deliveryFee;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private UUID orderItemId;
        private UUID menuId;
        private Integer quantity;
        private Integer priceAtOrder;
    }

    public static ResGetOrderDtoV1 from(OrderEntity order) {
        return ResGetOrderDtoV1.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .addressId(order.getAddressId())
                .request(order.getRequest())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .deliveryFee(order.getDeliveryFee())
                .createdAt(order.getCreatedAt())
                .orderItems(order.getOrderItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .orderItemId(item.getOrderItemId())
                                .menuId(item.getMenuId())
                                .quantity(item.getQuantity())
                                .priceAtOrder(item.getPriceAtOrder())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
