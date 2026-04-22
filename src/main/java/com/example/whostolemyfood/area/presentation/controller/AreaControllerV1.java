package com.example.whostolemyfood.area.presentation.controller;

import com.example.whostolemyfood.area.application.service.AreaServiceV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.request.ReqUpdateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResCreateAreaDtoV1;
import com.example.whostolemyfood.area.presentation.dto.response.ResGetAreaDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/areas")
public class AreaControllerV1 {

	private final AreaServiceV1 areaServiceV1;

	@PostMapping
	//@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResCreateAreaDtoV1> createArea(
		@Valid @RequestBody ReqCreateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.createArea(reqDto));
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
	public ResponseEntity<ResGetAreaDtoV1> getArea(
		@PathVariable UUID areaId
	) {
		return ResponseEntity.ok(areaServiceV1.getArea(areaId));
	}

	@PutMapping("/{areaId}")
	//@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateArea(
		@PathVariable UUID areaId,
		@Valid @RequestBody ReqUpdateAreaDtoV1 reqDto
	) {
		return ResponseEntity.ok(areaServiceV1.updateArea(areaId, reqDto));
	}

	@PatchMapping("/{areaId}/active")
	//@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<ResGetAreaDtoV1> updateAreaActive(
		@PathVariable UUID areaId,
		@RequestParam Boolean isActive
	) {
		return ResponseEntity.ok(areaServiceV1.updateAreaActive(areaId, isActive));
	}

	@DeleteMapping("/{areaId}")
	//@PreAuthorize("hasAnyRole('MANAGER', 'MASTER')")
	public ResponseEntity<Void> deleteArea(
		@PathVariable UUID areaId
	) {
		areaServiceV1.deleteArea(areaId);
		return ResponseEntity.noContent().build();
	}
}