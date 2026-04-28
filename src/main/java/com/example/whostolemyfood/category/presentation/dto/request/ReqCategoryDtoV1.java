package com.example.whostolemyfood.category.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "카테고리 요청 객체")
@NoArgsConstructor
@AllArgsConstructor
public class ReqCategoryDtoV1 {

    @Schema(description = "카테고리명", example = "치킨")
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String name;
}
