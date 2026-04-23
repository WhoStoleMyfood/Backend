package com.example.whostolemyfood.area.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReqCreateAreaDtoV1 {

	@NotBlank(message = "지역명은 필수입니다.")
	private String ukName;

	@NotBlank(message = "시는 필수입니다.")
	private String city;

	@NotBlank(message = "구는 필수입니다.")
	private String district;
}