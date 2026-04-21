package com.example.whostolemyfood.menu.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReqCreateMenuDtoV1 {

    private UUID storeId;
    private String menuName;
    private Integer menuPrice;
    private String menuDescription;
}
