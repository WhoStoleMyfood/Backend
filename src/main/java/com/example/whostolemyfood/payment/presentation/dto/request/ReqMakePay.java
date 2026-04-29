package com.example.whostolemyfood.payment.presentation.dto.request;

import com.example.whostolemyfood.payment.domain.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Order의 Id값 UUID", example = "f71f79d1-f367-48f7-b5ab-7a16d8317cf6")
    private String orderId;

    @NotNull
    @Positive
    @Schema(description = "가격정보", example = "50000")
    private Long amount;

    @NotBlank(message = "결제 정보는 필수입니다.")
    @Schema(description = "결제서버에서 발급해주는 paymentKey값", example = "tgen_202604271716451djO0")
    private String paymentKey;

    @NotBlank(message = "주문 정보는 필수입니다.")
    @Schema(description = "주문 상품명", example = "튼튼밀크")
    private String orderName;

    @Schema(description = "결제 방법타입", example = "CARD")
    private PaymentType payType = PaymentType.CARD;

}
