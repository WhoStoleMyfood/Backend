package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
public class ResCreateStoreDtoV1 {
    private UUID id;
    private String storeName;
    private String address;
    private String phone;
    private String content;
    private Integer minOrderPrice;
    private LocalTime openTime;
    private LocalTime closeTime;

    public ResCreateStoreDtoV1(UUID id, String storeName, String address, String phone, String content, Integer minOrderPrice, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.storeName = storeName;
        this.address = address;
        this.phone = phone;
        this.content = content;
        this.minOrderPrice = minOrderPrice;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public static ResCreateStoreDtoV1 from(StoreEntity store) {
        return new ResCreateStoreDtoV1(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getContent(),
                store.getMinOrderPrice(),
                store.getOpenTime(),
                store.getCloseTime());
    }
}