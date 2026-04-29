package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResGetStoreListDtoV1 {

    private UUID storeId;
    private String name;
    private String category;
    private BigDecimal averageRating;
    private String ukName;
    private Integer minOrderPrice;
    private StoreStatus storeStatus;
    private LocalTime openTime;
    private LocalTime closeTime;

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
                store.getStoreId(),
                store.getName(),
                store.getCategory().getName(),
                store.getStoreRatingSummary().getAverageRating(),
                store.getArea().getUkName(),
                store.getMinOrderPrice(),
                currentStatus,
                store.getOpenTime(),
                store.getCloseTime()
        );
    }
}
