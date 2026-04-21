package com.example.whostolemyfood.store.domain.entity;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum StoreStatus {
    OPEN("영업중"),
    CLOSED("영업종료"),
    PREPARING("영업준비중"),
    SHUTDOWN("폐점");

    private final String description;

    StoreStatus(String description) {
        this.description = description;
    }

    public static StoreStatus calculateStatus(LocalTime now, LocalTime open, LocalTime close) {

        LocalTime preparingTime = open.minusHours(2);

        if (now.isBefore(open)&&(now.isAfter(preparingTime) || now.equals(preparingTime))) {
            return PREPARING;
        }

        if (now.isAfter(open)&&(now.equals(open) || now.isBefore(close))) {
            return OPEN;
        }
        return CLOSED;
    }
}
