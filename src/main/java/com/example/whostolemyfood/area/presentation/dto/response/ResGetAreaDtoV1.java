package com.example.whostolemyfood.area.presentation.dto.response;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResGetAreaDtoV1 {

	private UUID areaId;
	private String ukName;
	private String city;
	private String district;
	private Boolean isActive;

	public static ResGetAreaDtoV1 from(AreaEntity areaEntity) {
		return ResGetAreaDtoV1.builder()
			.areaId(areaEntity.getAreaId())
			.ukName(areaEntity.getUkName())
			.city(areaEntity.getCity())
			.district(areaEntity.getDistrict())
			.isActive(areaEntity.getIsActive())
			.build();
	}
}