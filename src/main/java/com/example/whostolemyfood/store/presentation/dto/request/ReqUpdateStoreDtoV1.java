package com.example.whostolemyfood.store.presentation.dto.request;

import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class ReqUpdateStoreDtoV1 {

    private String storeName;
    private String address;
    private String phone;
    private String content;
    private Integer minOrderPrice;

    private StoreStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;
//    private Boolean isHidden;
//    private Boolean isDeleted;
}