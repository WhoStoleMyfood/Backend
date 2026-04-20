package com.example.whostolemyfood.store.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReqCreateStoreDtoV1 {

//    private UUID userId;
    private String storeName;
    private String address;
    private String phone;
    private String content;
    private Integer minOrderPrice;
}
