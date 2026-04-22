package com.example.whostolemyfood.payment.presentation.dto.request;

import com.example.whostolemyfood.payment.domain.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReqModifyPay {

    @NotBlank(message = "결제 정보는 필수입니다.")
    private String paymentId;

    @NotNull(message = "상태 정보는 필수입니다.")
    private PaymentStatus paymentStatus;
}
