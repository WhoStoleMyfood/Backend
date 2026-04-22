package com.example.whostolemyfood.address.presentation.dto.response;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import lombok.*;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ResCreateAddressDtoV1 {
    private UUID addressId;

    public static ResCreateAddressDtoV1 from(AddressEntity entity) {
        return ResCreateAddressDtoV1.builder()
                .addressId(entity.getId())
                .build();
    }
}
