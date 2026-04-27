package com.example.whostolemyfood.area.presentation.controller;

import com.example.whostolemyfood.area.application.service.AreaServiceV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/areas")
public class AreaControllerV1 {

	private final AreaServiceV1 areaServiceV1;

	@PostMapping
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResCreateAreaDtoV1> createArea(
		@AuthenticationPrincipal AuthUser loginUser,
		@Valid @RequestBody ReqCreateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.createArea(loginUser, reqDto));
	}

	@GetMapping
	public ResponseEntity<List<ResGetAreaDtoV1>> getAllAreas() {
		return ResponseEntity.ok(areaServiceV1.getAllAreas());
	}

	@GetMapping("/active")
	public ResponseEntity<List<ResGetAreaDtoV1>> getActiveAreas() {
		return ResponseEntity.ok(areaServiceV1.getActiveAreas());
	}

	@GetMapping("/{areaId}")
	public ResponseEntity<ResGetAreaDtoV1> getArea(@PathVariable UUID areaId) {
		return ResponseEntity.ok(areaServiceV1.getArea(areaId));
	}

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

	@PutMapping("/{areaId}")
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateArea(
		@AuthenticationPrincipal AuthUser loginUser,
		@PathVariable UUID areaId,
		@Valid @RequestBody ReqUpdateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.updateArea(loginUser, areaId, reqDto));
	}

	@PatchMapping("/{areaId}/active")
	@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateAreaActive(
		@AuthenticationPrincipal AuthUser loginUser,
		@PathVariable UUID areaId,
		@RequestParam Boolean isActive
	) {
		return ResponseEntity.ok(areaServiceV1.updateAreaActive(loginUser, areaId, isActive));
	}

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