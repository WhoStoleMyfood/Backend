package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResGetInActiveStoreDtoV1 {

    private UUID storeId;
    private UUID ownerId;
    private String name;
    private String address;
    private String phone;
    private String content;
    private String category;
    private String ukName;
    private Integer minOrderPrice;
    private StoreStatus storeStatus;
    private LocalTime openTime;
    private LocalTime closeTime;

    private Boolean isHidden;
    private Boolean isDeleted;

    public static ResGetInActiveStoreDtoV1 from(StoreEntity store) {
        return new ResGetInActiveStoreDtoV1(
                store.getStoreId(),
                store.getUser().getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getContent(),
                store.getCategory().getName(),
                store.getArea().getUkName(),
                store.getMinOrderPrice(),
                store.getCalculatedStatus(),
                store.getOpenTime(),
                store.getCloseTime(),
                store.getIsHidden(),
                store.getIsDeleted());
    }
}
