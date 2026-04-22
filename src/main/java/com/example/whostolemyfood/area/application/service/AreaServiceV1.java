package com.example.whostolemyfood.area.application.service;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AreaServiceV1 {

	private final AreaRepository areaRepository;

	@Transactional
	public ResCreateAreaDtoV1 createArea(ReqCreateAreaDtoV1 reqDto) {
		validateDuplicateUkName(reqDto.getUkName());

		AreaEntity areaEntity = AreaEntity.builder()
			.ukName(reqDto.getUkName())
			.city(reqDto.getCity())
			.district(reqDto.getDistrict())
			.isActive(true)
			.build();

		AreaEntity savedArea = areaRepository.save(areaEntity);
		return ResCreateAreaDtoV1.from(savedArea);
	}

	public List<ResGetAreaDtoV1> getAllAreas() {
		return areaRepository.findAll()
			.stream()
			.map(ResGetAreaDtoV1::from)
			.toList();
	}

	public List<ResGetAreaDtoV1> getActiveAreas() {
		return areaRepository.findAllByIsActiveTrue()
			.stream()
			.map(ResGetAreaDtoV1::from)
			.toList();
	}

	public ResGetAreaDtoV1 getArea(UUID areaId) {
		AreaEntity areaEntity = findAreaById(areaId);
		return ResGetAreaDtoV1.from(areaEntity);
	}

	@Transactional
	public ResGetAreaDtoV1 updateArea(UUID areaId, ReqUpdateAreaDtoV1 reqDto) {
		AreaEntity areaEntity = findAreaById(areaId);

		if (!areaEntity.getUkName().equals(reqDto.getUkName())) {
			validateDuplicateUkName(reqDto.getUkName());
		}

		areaEntity.update(
			reqDto.getUkName(),
			reqDto.getCity(),
			reqDto.getDistrict()
		);

		return ResGetAreaDtoV1.from(areaEntity);
	}

	@Transactional
	public ResGetAreaDtoV1 updateAreaActive(UUID areaId, Boolean isActive) {
		AreaEntity areaEntity = findAreaById(areaId);
		areaEntity.updateActive(isActive);
		return ResGetAreaDtoV1.from(areaEntity);
	}

	@Transactional
	public void deleteArea(UUID areaId) {
		AreaEntity areaEntity = findAreaById(areaId);
		areaRepository.delete(areaEntity);
	}

	private AreaEntity findAreaById(UUID areaId) {
		return areaRepository.findByAreaId(areaId)
			.orElseThrow(() -> new EntityNotFoundException("해당 지역을 찾을 수 없습니다. id=" + areaId));
	}

	private void validateDuplicateUkName(String ukName) {
		if (areaRepository.existsByUkName(ukName)) {
			throw new IllegalArgumentException("이미 존재하는 지역명입니다.");
		}
	}
}