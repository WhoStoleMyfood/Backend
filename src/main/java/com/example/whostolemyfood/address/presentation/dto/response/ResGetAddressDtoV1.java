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
    private LocalDateTime createdAt;
    private String message;

    // 메시지 없이 변환할 때 (기본 호출)
    public static ResGetAddressDtoV1 from(AddressEntity entity) {
        return from(entity, null); // 아래 메서드를 호출하여 중복 제거
    }

    // 메시지를 포함하여 변환할 때
    public static ResGetAddressDtoV1 from(AddressEntity entity, String message) {
        return ResGetAddressDtoV1.builder()
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
