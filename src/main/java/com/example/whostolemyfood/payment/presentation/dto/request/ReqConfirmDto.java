package com.example.whostolemyfood.payment.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ReqConfirmDto {

    @NotBlank
    @Schema(description = "토스 서버에서 발급해주는 paymentKey값", example = "tgen_202604271716451djO0")
    private String paymentKey;

    @NotBlank
    @Schema(description = "order의 Id값 UUID", example = "f71f79d1-f367-48f7-b5ab-7a16d8317cf6")
    private String orderId;

    @Positive
    @Schema(description = "가격정보", example = "50000")
    private Long amount;
}
