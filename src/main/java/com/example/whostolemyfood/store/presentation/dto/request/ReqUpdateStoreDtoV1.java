package com.example.whostolemyfood.store.presentation.dto.request;

import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class ReqUpdateStoreDtoV1 {

    @NotBlank(message = "가게 이름은 필수입니다")
    private String name;

    @NotBlank(message = "가게 주소는 필수입니다")
    private String address;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;

    private String content;

    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다")
    private Integer minOrderPrice;

    @NotNull(message = "영업 시작시간은 필수입니다")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "영업 종료시간은 필수입니다")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    private StoreStatus status;
//    private Boolean isHidden;
//    private Boolean isDeleted;
}
