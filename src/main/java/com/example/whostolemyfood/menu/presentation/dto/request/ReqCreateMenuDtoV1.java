package com.example.whostolemyfood.menu.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "메뉴 추가 요청 객체")
@NoArgsConstructor
public class ReqCreateMenuDtoV1 {

    @Schema(description = "메뉴명", example = "황금올리브 치킨")
    @NotBlank(message = "메뉴 이름은 필수입니다")
    private String name;

    @Schema(description = "메뉴 가격", example = "23000")
    @Min(value = 0, message = "메뉴 최소 선정금액은 0원 이상이여야 합니다")
    private Integer price;

    @Schema(description = "메뉴 설명(직접작성)", example = "올리브유에 튀긴 치킨")
    private String description;
    @Schema(description = "AI 추천 설명 사용 여부", example = "false")
    private Boolean aiDescription;
    @Schema(description = "AI 추천 설명용 프롬포트")
    private String aiPrompt;
}
