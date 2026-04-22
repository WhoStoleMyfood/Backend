package com.example.whostolemyfood.payment.presentation.dto.request;

import com.example.whostolemyfood.payment.domain.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReqMakePay {

    @NotBlank(message = "주문 Id는 필수입니다.")
    private String orderId;

    @NotNull
    @Positive
    private Long amount;

    @NotBlank(message = "결제 정보는 필수입니다.")
    private String paymentKey;

    @NotBlank(message = "주문 정보는 필수입니다.")
    private String orderName;

    private PaymentType payType = PaymentType.CARD;

}
