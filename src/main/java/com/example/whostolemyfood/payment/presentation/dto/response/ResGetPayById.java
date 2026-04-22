package com.example.whostolemyfood.payment.presentation.dto.response;

import com.example.whostolemyfood.payment.domain.PaymentEntity;
import com.example.whostolemyfood.payment.domain.PaymentStatus;
import com.example.whostolemyfood.payment.domain.PaymentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ResGetPayById {

    private UUID paymentId;

    private String paymentKey;

    private PaymentStatus payStatus;

    private PaymentType payType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime approvedAt;

    private UUID OrderId;

    private Long finalAmount;

    public ResGetPayById(PaymentEntity payment) {
        this.paymentId = payment.getId();
        this.paymentKey = payment.getPaymentKey();
        this.payStatus = payment.getPayStatus();
        this.payType = payment.getPayType();
        this.approvedAt = payment.getApprovedAt();
        this.OrderId = payment.getOrder().getOrderId();
        this.finalAmount = payment.getAmount();
    }

}
