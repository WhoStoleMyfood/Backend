package com.example.whostolemyfood.order.presentation.dto.response;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import lombok.*;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResCreateOrderDtoV1 {
    private UUID orderId;
    private Integer totalPrice;
    private String message;

    public static ResCreateOrderDtoV1 from(OrderEntity order) {
        return ResCreateOrderDtoV1.builder()
                .orderId(order.getOrderId())
                .totalPrice(order.getTotalPrice())
                .message("주문이 성공적으로 생성되었습니다.")
                .build();
    }
}
