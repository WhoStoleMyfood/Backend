package com.example.whostolemyfood.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {

    READY("결제 생성"),
    IN_PROGRESS("결제 인증 완료/승인 대기"),
    DONE("결제 완료"),
    CANCELED("결제 취소"),
    FAIL("결제 실패"),
    EXPIRED("시간 초과");

    private final String description;

}