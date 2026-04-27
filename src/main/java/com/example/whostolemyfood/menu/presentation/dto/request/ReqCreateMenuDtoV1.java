package com.example.whostolemyfood.menu.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReqCreateMenuDtoV1 {

    @NotBlank(message = "메뉴 이름은 필수입니다")
    private String name;
    @Min(value = 0, message = "메뉴 최소 선정금액은 0원 이상이여야 합니다")
    private Integer price;

    private String description;
    private Boolean aiDescription;
    private String aiPrompt;
}
