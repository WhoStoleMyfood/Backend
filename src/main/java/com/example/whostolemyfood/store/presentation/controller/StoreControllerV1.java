package com.example.whostolemyfood.store.presentation.controller;

import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.store.application.service.StoreSearchServiceV1;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.*;
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

@Tag(name = "Store API", description = "가게 관리 API")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreControllerV1 {

    private final StoreServiceV1  storeServiceV1;
    private final StoreSearchServiceV1 storeSearchService;

    // Owner Only
    @Operation(summary = "가게 생성", description = "[OWNER] 가게를 등록합니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ResCreateStoreDtoV1> createStore(
            @Valid @RequestBody ReqCreateStoreDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResCreateStoreDtoV1 response = storeServiceV1.createStore(request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ALL
    @Operation(summary = "가게 조회", description = "[ALL] 특정 가게를 조회합니다.")
    @GetMapping("/{storeId}")
    public ResponseEntity<ResGetStoreDtoV1> getStore(@PathVariable UUID storeId) {
        ResGetStoreDtoV1 response = storeServiceV1.getStore(storeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "가게 목록조회", description = "[ALL] 모든 가게 목록을 조회합니다.")
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<PageResponse<ResGetStoreListDtoV1>> getStores(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetStoreListDtoV1> stores = storeServiceV1.getStores(validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(stores));
    }

    // Owner, manager, master
    @Operation(summary = "가게 수정", description = "[OWNER / MANAGER / MASTER] 가게의 정보를 수정합니다.")
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ResGetStoreDtoV1> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody ReqUpdateStoreDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResGetStoreDtoV1 response = storeServiceV1.updateStore(storeId, request, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "가게 숨김 / 노출", description = "[OWNER / MANAGER / MASTER] 가게를 숨기거나 노출시킬 수 있습니다.")
    @PatchMapping("/{storeId}/hide")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<Void> hideStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal AuthUser authUser) {
        storeServiceV1.hiddenStore(storeId, authUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "숨겨지거나 삭제된 가게 조회", description = "[OWNER / MANAGER / MASTER] 숨겨지거나 삭제된 가게를 조회할 수 있습니다.")
    @GetMapping("/inactive")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<PageResponse<ResGetInActiveStoreDtoV1>> getInactiveStores(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetInActiveStoreDtoV1> stores = storeServiceV1.getInActiveStores(authUser, validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(stores));
    }

    @Operation(summary = "가게 삭제", description = "[OWNER / MANAGER / MASTER]] 가게를 삭제시킵니다.")
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public void deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal AuthUser authUser) {
        storeServiceV1.deleteStore(storeId, authUser);
    }

    @Operation(summary = "조건별 조회", description = "[ALL] 가게명,카테고리,지역명으로 가게를 조회합니다.")
    @PageableAsQueryParam
    @GetMapping("/search")
    public ResponseEntity<PageResponse<StoreSearchResponseDtoV1>> search(
            @ModelAttribute StoreSearchConditionV1 condition,
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        // 1. PageUtil을 사용하여 페이지 사이즈 검증 및 보정
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);

        // 2. 서비스 호출 및 결과 반환
        PageResponse<StoreSearchResponseDtoV1> response = storeSearchService.search(condition, validatedPageable,authUser.getUserId(), authUser.role().name());
        return ResponseEntity.ok(response);
    }
}
