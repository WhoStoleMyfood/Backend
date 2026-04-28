package com.example.whostolemyfood.area.presentation.controller;

import com.example.whostolemyfood.area.application.service.AreaServiceV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Area API", description = "운영지역 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/areas")
public class AreaControllerV1 {

	private final AreaServiceV1 areaServiceV1;

	@Operation(summary = "운영지역 생성", description = "[MANAGER / MASTER] 서비스 운영지역을 생성합니다.")
	@PostMapping
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResCreateAreaDtoV1> createArea(
		@AuthenticationPrincipal AuthUser loginUser,
		@Valid @RequestBody ReqCreateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.createArea(loginUser, reqDto));
	}

	@Operation(summary = "모든 운영지역 조회", description = "[ALL] 모든 운영지역을 조회합니다.")
	@GetMapping
	public ResponseEntity<List<ResGetAreaDtoV1>> getAllAreas() {
		return ResponseEntity.ok(areaServiceV1.getAllAreas());
	}

	@Operation(summary = "활성화된 운영지역 조회", description = "[ALL] 현재 서비스 활성화가 된 지역을 조회합니다.")
	@GetMapping("/active")
	public ResponseEntity<List<ResGetAreaDtoV1>> getActiveAreas() {
		return ResponseEntity.ok(areaServiceV1.getActiveAreas());
	}

	@Operation(summary = "특정 지역 상세 정보 조회", description = "[ALL] 특정 지역의 정보를 조회합니다.")
	@GetMapping("/{areaId}")
	public ResponseEntity<ResGetAreaDtoV1> getArea(@PathVariable UUID areaId) {
		return ResponseEntity.ok(areaServiceV1.getArea(areaId));
	}

	@Operation(summary = "지역 조건 검색 및 목록 조회", description = "[ALL] 시(city), 구(district), 활성화 여부를 조건으로 지역을 검색합니다.")
	@GetMapping("/search")
	public ResponseEntity<Page<ResGetAreaDtoV1>> searchAreas(
		@RequestParam(required = false) String city,
		@RequestParam(required = false) String district,
		@RequestParam(required = false) Boolean isActive,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "desc") String sortDir
	) {
		return ResponseEntity.ok(
			areaServiceV1.searchAreas(city, district, isActive, page, size, sortDir)
		);
	}

	@Operation(summary = "지역 정보 수정", description = "[MANGER / MASTER] 지역정보를 수정합니다.")
	@PutMapping("/{areaId}")
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateArea(
		@AuthenticationPrincipal AuthUser loginUser,
		@PathVariable UUID areaId,
		@Valid @RequestBody ReqUpdateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.updateArea(loginUser, areaId, reqDto));
	}

	@Operation(summary = "지역 활성화 상태 변경", description = "[MANAGER / MASTER] 특정 지역의 활성화 여부를 변경합니다.")
	@PatchMapping("/{areaId}/active")
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateAreaActive(
		@AuthenticationPrincipal AuthUser loginUser,
		@PathVariable UUID areaId,
		@RequestParam Boolean isActive
	) {
		return ResponseEntity.ok(areaServiceV1.updateAreaActive(loginUser, areaId, isActive));
	}

	@Operation(summary = "지역 정보 삭제", description = "[MANAGER / MASTER] 지정한 지역 정보를 삭제합니다")
	@DeleteMapping("/{areaId}")
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<Void> deleteArea(
		@AuthenticationPrincipal AuthUser loginUser,
		@PathVariable UUID areaId
	) {
		areaServiceV1.deleteArea(loginUser, areaId);
		return ResponseEntity.noContent().build();
	}
}