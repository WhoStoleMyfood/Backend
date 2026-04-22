package com.example.whostolemyfood.address.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateAddressDtoV1 {

    private String alias;

    @NotBlank(message = "배송지 주소는 필수 입력 사항입니다.")
    private String address;

    private String detail;

    private String zipCode;

    private Boolean isDefault;
}
