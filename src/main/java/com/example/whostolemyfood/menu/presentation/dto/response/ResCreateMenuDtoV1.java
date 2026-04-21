package com.example.whostolemyfood.menu.presentation.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ResCreateMenuDtoV1 {

    private UUID storeId;
    private UUID menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuDescription;

}
