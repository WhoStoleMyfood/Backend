package com.example.whostolemyfood.area.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Schema(description = "운영지역 생성 요청 객체")
@NoArgsConstructor
public class ReqCreateAreaDtoV1 {

	@Schema(description = "지역명", example = "광화문")
	@NotBlank(message = "지역명은 필수입니다.")
	private String ukName;

	@Schema(description = "시 / 도", example = "서울특별시")
	@NotBlank(message = "시는 필수입니다.")
	private String city;

	@Schema(description = "구 / 군", example = "종로구")
	@NotBlank(message = "구는 필수입니다.")
	private String district;
}