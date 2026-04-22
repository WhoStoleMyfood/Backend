package com.example.whostolemyfood.category.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateCategoryDtoV1 {
    @NotNull
    private UUID categoryId;

    @NotBlank(message = "카테고리명은 필수입니다.")
    private String name;
}
