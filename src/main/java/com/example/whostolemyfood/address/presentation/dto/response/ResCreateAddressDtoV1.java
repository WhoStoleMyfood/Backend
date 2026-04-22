package com.example.whostolemyfood.address.presentation.dto.response;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResCreateAddressDtoV1 {
    private UUID addressId;
    private String alias;
    private String address;
    private String detail;
    private String zipCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private String message;

    public static ResCreateAddressDtoV1 from(AddressEntity entity, String message) {
        return ResCreateAddressDtoV1.builder()
                .addressId(entity.getId())
                .alias(entity.getAlias())
                .address(entity.getAddress())
                .detail(entity.getDetail())
                .zipCode(entity.getZipCode())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .message(message)
                .build();
    }
}
