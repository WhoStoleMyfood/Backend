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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
			.orElseThrow(() -> new CustomException(ErrorCode.AREA_NOT_FOUND));
	}

	private void validateDuplicateUkName(String ukName) {
		if (areaRepository.existsByUkName(ukName)) {
			throw new CustomException(ErrorCode.AREA_DUPLICATION);
		}
	}

	private void validateAreaManageAuthority(AuthUser loginUser) {
		UserEntity user = userRepository.findById(loginUser.userId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (Boolean.TRUE.equals(user.getIsDeleted())) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		if (user.getUserRole() != loginUser.role()) {
			throw new CustomException(ErrorCode.AREA_ACCESS_DENIED);
		}

		if (user.getUserRole() != UserRole.MANAGER && user.getUserRole() != UserRole.MASTER) {
			throw new CustomException(ErrorCode.AREA_ACCESS_DENIED);
		}
	}

	public Page<ResGetAreaDtoV1> searchAreas(
		String city,
		String district,
		Boolean isActive,
		int page,
		int size,
		String sortDir
	) {
		int validSize = validatePageSize(size);

		Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
			? Sort.Direction.ASC
			: Sort.Direction.DESC;

		Pageable pageable = PageRequest.of(
			page,
			validSize,
			Sort.by(direction, "createdAt")
		);

		return areaRepository.searchAreas(city, district, isActive, pageable)
			.map(ResGetAreaDtoV1::from);
	}

	private int validatePageSize(int size) {
		if (size == 10 || size == 30 || size == 50) {
			return size;
		}
		return 10;
	}
}