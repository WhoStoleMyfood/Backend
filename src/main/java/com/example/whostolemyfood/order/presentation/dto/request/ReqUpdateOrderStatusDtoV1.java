package com.example.whostolemyfood.order.presentation.dto.request;

import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Schema(description = "주문 상태 변경 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateOrderStatusDtoV1 {

    @Schema(description = "주문 상태")
    @NotNull(message = "변경할 상태 정보는 필수입니다.")
    private OrderStatus status;
}
