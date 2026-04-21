package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
public class ResGetStoreListDtoV1 {

    private UUID storeId;
    private String storeName;
    private Integer minOrderPrice;
    private StoreStatus storeStatus;
    private LocalTime openTime;
    private LocalTime closeTime;

    public ResGetStoreListDtoV1(UUID storeId,String storeName, Integer minOrderPrice, StoreStatus storeStatus, LocalTime openTime, LocalTime closeTime) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.minOrderPrice = minOrderPrice;
        this.storeStatus = storeStatus;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public static ResGetStoreListDtoV1 from(StoreEntity store) {
        StoreStatus currentStatus;

        if (store.getStatus() == StoreStatus.SHUTDOWN) {
            currentStatus = StoreStatus.SHUTDOWN;
        } else {
            currentStatus = StoreStatus.calculateStatus(
                    LocalTime.now(),
                    store.getOpenTime(),
                    store.getCloseTime()
            );
        }
        return new ResGetStoreListDtoV1(
                store.getId(),
                store.getName(),
                store.getMinOrderPrice(),
                currentStatus,
                store.getOpenTime(),
                store.getCloseTime()
        );
    }
}
