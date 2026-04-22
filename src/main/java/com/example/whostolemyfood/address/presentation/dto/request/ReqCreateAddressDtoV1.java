package com.example.whostolemyfood.address.presentation.dto.request;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqCreateAddressDtoV1 {

    private String alias;

    @NotBlank(message = "배송지 주소는 필수 입력 사항입니다.")
    private String address;

    private String detail;

    private String zipCode;

    private Boolean isDefault;

    public AddressEntity toEntity(UUID userId) {
        return AddressEntity.builder()
                .userId(userId)
                .alias(this.alias)
                .address(this.address)
                .detail(this.detail)
                .zipCode(this.zipCode)
                .isDefault(this.isDefault != null ? this.isDefault : false)
                .build();
    }
}
