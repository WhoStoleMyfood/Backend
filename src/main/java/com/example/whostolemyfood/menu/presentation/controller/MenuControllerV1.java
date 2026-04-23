package com.example.whostolemyfood.menu.presentation.controller;

import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuServiceV1;

    @Operation(summary = "메뉴 생성")
    @PostMapping
    public ResponseEntity<ResCreateMenuDtoV1> addMenu(@Valid @PathVariable UUID storeId, @RequestBody ReqCreateMenuDtoV1 request) {
        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "메뉴 조회")
    @GetMapping("/{menuId}")
    public ResponseEntity<ResGetMenuDtoV1> getMenu(
            @Valid
            @PathVariable UUID storeId,
            @PathVariable UUID menuId) {
        ResGetMenuDtoV1 response = menuServiceV1.getMenu(storeId, menuId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "메뉴 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<ResGetMenuDtoV1>> getMenus(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetMenuDtoV1> menus = menuServiceV1.getMenus(validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(menus));
    }

    @Operation(summary = "메뉴 수정")
    @PutMapping("/{menuId}")
    public ResponseEntity<ResGetMenuDtoV1> updateMenu(
            @Valid
            @PathVariable UUID storeId ,
            @PathVariable UUID menuId,
            @RequestBody ReqUpdateMenuDtoV1 request) {
        ResGetMenuDtoV1 response = menuServiceV1.updateMenu(storeId, menuId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "메뉴 숨김 / 해제")
    @PatchMapping("/{menuId}/hide")
    public ResponseEntity<Void> hideMenu(@PathVariable UUID storeId, @PathVariable UUID menuId) {
        menuServiceV1.hiddenMenu(storeId,menuId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "메뉴 삭제")
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@Valid @PathVariable UUID storeId, @PathVariable UUID menuId) {
        menuServiceV1.deleteMenu(storeId, menuId);
    }
}
