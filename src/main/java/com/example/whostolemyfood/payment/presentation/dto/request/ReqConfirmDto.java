package com.example.whostolemyfood.payment.presentation.dto.request;

import lombok.Getter;

@Getter
public class ReqConfirmDto {

    private String paymentKey;

    private String orderId;

    private Long amount;
}
