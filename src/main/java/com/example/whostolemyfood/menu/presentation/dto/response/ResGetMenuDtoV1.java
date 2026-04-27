package com.example.whostolemyfood.menu.presentation.dto.response;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResGetMenuDtoV1 {

    private UUID storeId;
    private UUID menuId;
    private String name;
    private Integer price;
    private String description;

    public static ResGetMenuDtoV1 from(MenuEntity menu) {
        return new ResGetMenuDtoV1(
                menu.getStore().getStoreId(),
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription()
        );
    }
}
