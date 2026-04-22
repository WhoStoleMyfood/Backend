package com.example.whostolemyfood.payment.presentation.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResModifyPay {

    private UUID paymentId;

    private String receiptUrl = "http://cancelExam.com";

    private LocalDateTime approvedAt;

    public ResModifyPay(UUID id, LocalDateTime now) {
        this.paymentId = id;
        this.approvedAt = now;
    }
}
