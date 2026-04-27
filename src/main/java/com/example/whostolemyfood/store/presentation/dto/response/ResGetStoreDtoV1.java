package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResGetStoreDtoV1 {

    private UUID storeId;
    private String name;
    private String address;
    private String phone;
    private String content;
    private Integer minOrderPrice;
    private StoreStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;


    public static ResGetStoreDtoV1 from(StoreEntity store) {
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

        return new ResGetStoreDtoV1(
                store.getStoreId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getContent(),
                store.getMinOrderPrice(),
                currentStatus,
                store.getOpenTime(),
                store.getCloseTime()
        );
    }
}
