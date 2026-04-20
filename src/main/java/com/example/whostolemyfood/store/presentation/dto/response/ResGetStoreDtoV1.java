package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.Getter;

@Getter
public class ResGetStoreDtoV1 {

    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String content;
    private Integer minOrderPrice;
    private StoreStatus status;

    public ResGetStoreDtoV1(String storeName, String storeAddress, String storePhone, String content, Integer minOrderPrice, StoreStatus status) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storePhone = storePhone;
        this.content = content;
        this.minOrderPrice = minOrderPrice;
        this.status = status;
    }

    public static ResGetStoreDtoV1 from(StoreEntity store) {
        return new ResGetStoreDtoV1(
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getContent(),
                store.getMinOrderPrice(),
                store.getStatus());
    }
}
