package com.example.whostolemyfood.menu.presentation.dto.response;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResCreateMenuDtoV1 {

    private UUID storeId;
    private UUID menuId;
    private String name;
    private Integer price;
    private String description;

    public static ResCreateMenuDtoV1 from(MenuEntity menu) {
        return ResCreateMenuDtoV1.builder()
                .storeId(menu.getStore().getStoreId())
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .build();
//        return new ResCreateMenuDtoV1(
//                menu.getStore().getStoreId(),
//                menu.getMenuId(),
//                menu.getName(),
//                menu.getPrice(),
//                menu.getDescription()
//        );
    }
}
