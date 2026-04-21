package com.example.whostolemyfood.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCreateOrderDtoV1 {

    @NotNull(message = "가게 정보는 필수입니다.")
    private UUID storeId;

    @NotNull(message = "배송지 정보는 필수입니다.")
    private UUID addressId;

    private String request;

    @Valid // List 내부의 OrderItemRequest 객체들까지 유효성 검사 수행
    @NotEmpty(message = "주문 상품은 최소 하나 이상이어야 합니다.")
    private List<OrderItemRequest> orderItems;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        @NotNull(message = "메뉴 정보는 필수입니다.")
        private UUID menuId;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        private Integer quantity;

        @NotNull(message = "가격 정보는 필수입니다.")
        private Integer priceAtOrder;
    }
}
