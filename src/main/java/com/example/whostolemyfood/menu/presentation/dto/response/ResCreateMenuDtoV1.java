package com.example.whostolemyfood.menu.presentation.dto.response;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResCreateMenuDtoV1 {

    private UUID storeId;
    private UUID menuId;
    private String name;
    private Integer price;
    private String description;

    public static ResCreateMenuDtoV1 from(MenuEntity menu) {
        return new ResCreateMenuDtoV1(
                menu.getStore().getId(),
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription()
        );
    }
}
