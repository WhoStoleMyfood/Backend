package com.example.whostolemyfood.store.presentation.dto.request;

import lombok.Getter;

@Getter
public class StoreSearchConditionV1 {

    private String keyword;

    private String storeName;

    private String category;

    private String region;

    private int minOrderPrice;

    private String sortBy;

    //페이징 처리를 위한 값
    private int page;

}
