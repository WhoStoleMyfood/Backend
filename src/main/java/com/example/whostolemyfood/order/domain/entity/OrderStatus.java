package com.example.whostolemyfood.order.domain.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("주문요청"),      // CUSTOMER
    ACCEPTED("주문수락"),     // OWNER
    COOKING("조리완료"),      // OWNER
    DELIVERING("배송수령"),   // OWNER
    DELIVERED("배송완료"),    // OWNER
    COMPLETED("주문완료"),    // OWNER
    CANCELLED("주문취소");    // CUSTOMER (5분 이내) or MASTER

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}

