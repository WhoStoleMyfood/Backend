package com.example.whostolemyfood.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateOrderRequestDtoV1 {
    @NotBlank(message = "요청사항 내용은 비어있을 수 없습니다.")
    private String request;
}
