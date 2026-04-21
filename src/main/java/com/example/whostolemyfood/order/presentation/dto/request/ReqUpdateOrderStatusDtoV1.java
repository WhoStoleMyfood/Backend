package com.example.whostolemyfood.order.presentation.dto.request;

import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateOrderStatusDtoV1 {
    @NotNull(message = "변경할 상태 정보는 필수입니다.")
    private OrderStatus status;
}
