package com.example.whostolemyfood.menu.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReqUpdateMenuDtoV1 {
    @NotBlank(message = "메뉴 이름은 필수입니다")
    private String name;
    @Min(value = 0, message = "메뉴 최소 선정 금액은 0원 이상이여야합니다")
    private Integer price;
    private String description;
}
