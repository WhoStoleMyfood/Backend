package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResCreateStoreDtoV1 {

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

    public static ResCreateStoreDtoV1 from(StoreEntity store) {
        return ResCreateStoreDtoV1.builder()
                .storeId(store.getStoreId())
                .storeId(store.getStoreId())
                .ownerId(store.getUser().getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .content(store.getContent())
                .category(store.getCategory().getName())
                .ukName(store.getArea().getUkName())
                .minOrderPrice(store.getMinOrderPrice())
                .storeStatus(store.getCalculatedStatus())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .build();
//        return new ResCreateStoreDtoV1(
//                store.getStoreId(),
//                store.getUser().getId(),
//                store.getName(),
//                store.getAddress(),
//                store.getPhone(),
//                store.getContent(),
//                store.getCategory().getName(),
//                store.getArea().getUkName(),
//                store.getMinOrderPrice(),
//                store.getCalculatedStatus(),
//                store.getOpenTime(),
//                store.getCloseTime());
    }
}