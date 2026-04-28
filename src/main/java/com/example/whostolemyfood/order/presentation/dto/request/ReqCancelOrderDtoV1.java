package com.example.whostolemyfood.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Schema(description = "주문 취소 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCancelOrderDtoV1 {

    @Schema(description = "취소 사유", example = "다른 음식이 먹고 싶어졌어요.")
    private String cancelReason;
}
