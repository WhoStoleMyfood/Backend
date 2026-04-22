package com.example.whostolemyfood.category.presentation.dto.response;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResGetCategoryDtoV1 {
    private UUID categoryId;
    private String name;

    public static ResGetCategoryDtoV1 from(CategoryEntity categoryEntity) {
        return ResGetCategoryDtoV1.builder()
                .categoryId(categoryEntity.getCategoryId())
                .name(categoryEntity.getName())
                .build();
    }

}
