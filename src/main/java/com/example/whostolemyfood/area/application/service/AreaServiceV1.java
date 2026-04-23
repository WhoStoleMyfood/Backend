package com.example.whostolemyfood.area.application.service;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
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
	private final UserRepository userRepository;

	@Transactional
	public ResCreateAreaDtoV1 createArea(AuthUser loginUser, ReqCreateAreaDtoV1 reqDto) {
		validateAreaManageAuthority(loginUser);
		validateDuplicateUkName(reqDto.getUkName());

		AreaEntity areaEntity = AreaEntity.builder()
			.ukName(reqDto.getUkName())
			.city(reqDto.getCity())
			.district(reqDto.getDistrict())
			.isActive(true)
			.build();

		areaEntity.markCreatedBy(loginUser.userId());

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
	public ResGetAreaDtoV1 updateArea(AuthUser loginUser, UUID areaId, ReqUpdateAreaDtoV1 reqDto) {
		validateAreaManageAuthority(loginUser);

		AreaEntity areaEntity = findAreaById(areaId);

		if (!areaEntity.getUkName().equals(reqDto.getUkName())) {
			validateDuplicateUkName(reqDto.getUkName());
		}

		areaEntity.update(
			reqDto.getUkName(),
			reqDto.getCity(),
			reqDto.getDistrict()
		);
		areaEntity.markUpdatedBy(loginUser.userId());

		return ResGetAreaDtoV1.from(areaEntity);
	}

	@Transactional
	public ResGetAreaDtoV1 updateAreaActive(AuthUser loginUser, UUID areaId, Boolean isActive) {
		validateAreaManageAuthority(loginUser);

		AreaEntity areaEntity = findAreaById(areaId);
		areaEntity.updateActive(isActive);
		areaEntity.markUpdatedBy(loginUser.userId());

		return ResGetAreaDtoV1.from(areaEntity);
	}

	@Transactional
	public void deleteArea(AuthUser loginUser, UUID areaId) {
		validateAreaManageAuthority(loginUser);

		AreaEntity areaEntity = findAreaById(areaId);
		areaEntity.softDelete(loginUser.userId());
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

	private void validateAreaManageAuthority(AuthUser loginUser) {
		UserEntity user = userRepository.findById(loginUser.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (Boolean.TRUE.equals(user.getIsDeleted())) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		if (user.getUserRole() != loginUser.role()) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		if (user.getUserRole() != UserRole.MANAGER && user.getUserRole() != UserRole.MASTER) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}
}