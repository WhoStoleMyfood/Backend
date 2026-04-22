package com.example.whostolemyfood.address.presentation.dto.response;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResGetAddressDtoV1 {
    private UUID addressId;
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
    private LocalDateTime createdAt; // Order 스타일과 맞추기 위해 추가

    public static ResGetAddressDtoV1 from(AddressEntity entity) {
        return ResGetAddressDtoV1.builder()
                .addressId(entity.getId())
                .alias(entity.getAlias())
                .address(entity.getAddress())
                .detail(entity.getDetail())
                .zipCode(entity.getZipCode())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
