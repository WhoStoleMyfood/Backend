package com.example.whostolemyfood.store.presentation.dto.response;

import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class StoreSearchResponseDtoV1 {
    private UUID storeId;
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String content;
    private Integer minOrderPrice;
    private StoreStatus status;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDateTime createdAt;
    private BigDecimal averageRating;

    @QueryProjection
    @Builder
    public StoreSearchResponseDtoV1(
            UUID storeId,
            String storeName,
            String storeAddress,
            String storePhone,
            String content,
            Integer minOrderPrice,
            StoreStatus status,
            LocalTime openTime,
            LocalTime closeTime,
            LocalDateTime createdAt,
            BigDecimal averageRating
    ) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.storePhone = storePhone;
        this.content = content;
        this.minOrderPrice = minOrderPrice;
        this.status = status;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.createdAt = createdAt;
        this.averageRating = averageRating;
    }
}
