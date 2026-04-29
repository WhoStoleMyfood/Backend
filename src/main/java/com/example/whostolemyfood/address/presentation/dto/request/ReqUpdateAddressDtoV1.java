package com.example.whostolemyfood.address.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Schema(description = "배송지 수정 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReqUpdateAddressDtoV1 {

    @Schema(description = "별칭", example = "자취방")
    private String alias;

    @Schema(description = "주소", example = "서울특별시 종로구 사직로161")
    @NotBlank(message = "배송지 주소는 필수 입력 사항입니다.")
    private String address;

    @Schema(description = "상세주소", example = "경복궁")
    private String detail;

    @Schema(description = "우편번호", example = "03045")
    private String zipCode;

    @Schema(description = "기본 배송지 여부", example = "true")
    private Boolean isDefault;
}