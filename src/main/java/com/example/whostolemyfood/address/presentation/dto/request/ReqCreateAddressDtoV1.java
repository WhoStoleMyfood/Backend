package com.example.whostolemyfood.address.presentation.dto.request;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Schema(description = "배송지 등록 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCreateAddressDtoV1 {

    @Schema(description = "별칭", example = "우리집")
    private String alias;

    @Schema(description = "주소", example = "서울특별시 종로구 세종대로 172")
    @NotBlank(message = "배송지 주소는 필수 입력 사항입니다.")
    private String address;

    @Schema(description = "상세주소", example = "세종대왕 상 앞")
    private String detail;

    @Schema(description = "우편번호", example = "11182")
    private String zipCode;

    @Schema(description = "기본 배송지 여부", example = "false")
    private Boolean isDefault;

    public AddressEntity toEntity(UUID userId) {
        return AddressEntity.builder()
                .userId(userId)
                .alias(this.alias)
                .address(this.address)
                .detail(this.detail)
                .zipCode(this.zipCode)
                .isDefault(this.isDefault != null ? this.isDefault : false)
                // isDeleted는 부모(BaseSoftDeleteEntity)가 자동으로 false로 설정.
                .build();
    }
}
