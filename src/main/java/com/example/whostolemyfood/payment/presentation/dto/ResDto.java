package com.example.whostolemyfood.payment.presentation.dto;

import lombok.Getter;

@Getter
public class ResDto<T> {

    int status;

    String message;

    T data;

    public ResDto(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ResDto<T> success(T data) {
        return new ResDto(200, "success", data);
    }

}
