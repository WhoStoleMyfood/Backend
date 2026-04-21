package com.example.whostolemyfood.order.presentation.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCancelOrderDtoV1 {
    private String cancelReason;
}
