package com.example.whostolemyfood.store.presentation.dto.request;

import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Schema(description = "가게 수정 요청 객체")
public class ReqUpdateStoreDtoV1 {

    @Schema(description = "가게명", example = "광화문 오대리 치킨")
    @NotBlank(message = "가게 이름은 필수입니다")
    private String name;

    @Schema(description = "가게주소", example = "서울특별시 종로구 사직로77")
    @NotBlank(message = "가게 주소는 필수입니다")
    private String address;

    @Schema(description = "가게번호", example = "000-0000-0000")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;

    @Schema(description = "가게정보", example = "광화문 최고 맛집 치킨집")
    private String content;

    @Schema(description = "카테고리")
    private UUID categoryId;

    @Schema(description = "운영지역")
    private UUID areaId;

    @Schema(description = "최소 주문 금액", example = "20000")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다")
    private Integer minOrderPrice;

    @Schema(description = "영업 시작 시간", example = "10:00")
    @NotNull(message = "영업 시작시간은 필수입니다")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @Schema(description = "영업 종료 시간", example = "23:00")
    @NotNull(message = "영업 종료시간은 필수입니다")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    @Schema(description = "가게상태")
    private StoreStatus status;
}
