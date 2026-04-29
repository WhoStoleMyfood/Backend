package com.example.whostolemyfood.menu.presentation.dto.response;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResGetInActiveMenuDtoV1 {

    private UUID storeId;
    private UUID menuId;
    private String name;
    private Integer price;
    private String description;

    private Boolean isHidden;
    private Boolean isDeleted;

    public static ResGetInActiveMenuDtoV1 from(MenuEntity menu) {
        return new ResGetInActiveMenuDtoV1(
                menu.getStore().getStoreId(),
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getIsHidden(),
                menu.getIsDeleted());
    }
}
