package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResGetStoreDtoV1 {

    private UUID storeId;
    private String name;
    private String address;
    private String phone;
    private String content;
    private String category;
    private BigDecimal averageRating;
    private String ukName;
    private Integer minOrderPrice;
    private StoreStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;


    public static ResGetStoreDtoV1 from(StoreEntity store) {
        return ResGetStoreDtoV1.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .content(store.getContent())
                .category(store.getCategory().getName())
                .averageRating(store.getStoreRatingSummary().getAverageRating())
                .ukName(store.getArea().getUkName())
                .minOrderPrice(store.getMinOrderPrice())
                .status(store.getCalculatedStatus())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .build();

//        return new ResGetStoreDtoV1(
//                store.getStoreId(),
//                store.getName(),
//                store.getAddress(),
//                store.getPhone(),
//                store.getContent(),
//                store.getCategory().getName(),
//                store.getStoreRatingSummary().getAverageRating(),
//                store.getArea().getUkName(),
//                store.getMinOrderPrice(),
//                store.getCalculatedStatus(),
//                store.getOpenTime(),
//                store.getCloseTime()
//        );
    }
}
