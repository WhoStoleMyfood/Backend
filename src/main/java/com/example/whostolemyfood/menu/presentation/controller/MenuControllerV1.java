package com.example.whostolemyfood.menu.presentation.controller;

import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Menu API", description = "메뉴 관리 API")
@RestController
@RequestMapping("/api/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuServiceV1;

    @Operation(summary = "메뉴 추가", description = "[OWNER] 메뉴를 추가합니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ResCreateMenuDtoV1> addMenu(
            @Valid
            @PathVariable UUID storeId,
            @RequestBody ReqCreateMenuDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId, request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "메뉴 조회", description = "[ALL] 특정 매뉴를 조회합니다.")
    @GetMapping("/{menuId}")
    public ResponseEntity<ResGetMenuDtoV1> getMenu(
            @Valid
            @PathVariable UUID storeId,
            @PathVariable UUID menuId) {
        ResGetMenuDtoV1 response = menuServiceV1.getMenu(storeId, menuId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "메뉴 목록 조회", description = "[ALL] 메뉴 목록을 조회합니다.")
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<PageResponse<ResGetMenuDtoV1>> getMenus(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetMenuDtoV1> menus = menuServiceV1.getMenus(validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(menus));
    }

    @Operation(summary = "메뉴 수정", description = "[OWNER / MANAGER / MASTER] 특정 메뉴를 수정합니다.")
    @PutMapping("/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ResGetMenuDtoV1> updateMenu(
            @Valid
            @PathVariable UUID storeId ,
            @PathVariable UUID menuId,
            @RequestBody ReqUpdateMenuDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResGetMenuDtoV1 response = menuServiceV1.updateMenu(storeId, menuId, request, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "메뉴 숨김 / 해제", description = "[OWNER / MANAGER / MASTER] 특정 메뉴를 숨기거나 노출시킵니다.")
    @PatchMapping("/{menuId}/hide")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<Void> hideMenu(
            @Valid
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @AuthenticationPrincipal AuthUser authUser) {
        menuServiceV1.hiddenMenu(storeId, menuId, authUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "메뉴 삭제", description = "[OWNER / MANAGER / MASTER] 특정 메뉴를 삭제합니다.")
    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public void deleteMenu(
            @Valid
            @PathVariable UUID storeId,
            @PathVariable UUID menuId,
            @AuthenticationPrincipal AuthUser authUser) {
        menuServiceV1.deleteMenu(storeId, menuId, authUser);
    }
}
