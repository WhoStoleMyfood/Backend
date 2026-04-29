package com.example.whostolemyfood.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "주문 생성 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCreateOrderDtoV1 {

    @Schema(description = "가게 정보")
    @NotNull(message = "가게 정보는 필수입니다.")
    private UUID storeId;

    @Schema(description = "배송지 정보")
    @NotNull(message = "배송지 정보는 필수입니다.")
    private UUID addressId;

    @Schema(description = "요청사항", example = "문앞에 두고 가주세요.")
    private String request;

    @Schema(description = "주문 상품")
    @Valid // List 내부의 OrderItemRequest 객체들까지 유효성 검사 수행
    @NotEmpty(message = "주문 상품은 최소 하나 이상이어야 합니다.")
    private List<OrderItemRequest> orderItems;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        @Schema(description = "메뉴 정보")
        @NotNull(message = "메뉴 정보는 필수입니다.")
        private UUID menuId;

        @Schema(description = "메뉴 수량")
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        private Integer quantity;

        @Schema(description = "총 주문액")
        @NotNull(message = "가격 정보는 필수입니다.")
        private Integer priceAtOrder;
    }
}
