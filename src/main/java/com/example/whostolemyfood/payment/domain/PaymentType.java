package com.example.whostolemyfood.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {

    CARD("신용/체크카드");

    private final String description;
}
