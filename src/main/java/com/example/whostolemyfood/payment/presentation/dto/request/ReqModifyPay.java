package com.example.whostolemyfood.payment.presentation.dto.request;

import com.example.whostolemyfood.payment.domain.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReqModifyPay {

    @NotBlank(message = "결제 정보는 필수입니다.")
    @Schema(description = "결제의 ID값", example = "t71f79d1-f367-48f7-b5ab-7a16d8317cf6")
    private String paymentId;

    @NotNull(message = "상태 정보는 필수입니다.")
    @Schema(description = "결제 진행 상태", example = "READY")
    private PaymentStatus paymentStatus;
}
