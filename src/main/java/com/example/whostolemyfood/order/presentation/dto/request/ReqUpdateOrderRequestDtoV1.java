package com.example.whostolemyfood.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Schema(description = "주문 수정 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateOrderRequestDtoV1 {

    @Schema(description = "요청사항", example = "벨 누르지 말아주세요.")
    @NotBlank(message = "요청사항 내용은 비어있을 수 없습니다.")
    private String request;
}
