package com.example.whostolemyfood.payment.presentation.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ResMakePay {

    private UUID paymentId;

    private String paymentKey;

    private String receiptUrl = "http://examReceipt.com";

    public ResMakePay(UUID paymentId, String paymentKey) {
        this.paymentId = paymentId;
        this.paymentKey = paymentKey;
    }
}
