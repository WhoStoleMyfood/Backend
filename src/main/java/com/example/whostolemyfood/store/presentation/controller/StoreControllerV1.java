package com.example.whostolemyfood.store.presentation.controller;

import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.store.application.service.StoreSearchServiceV1;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreControllerV1 {

    private final StoreServiceV1  storeServiceV1;
    private final StoreSearchServiceV1 storeSearchService;

    // Owner Only
    @Operation(summary = "스토어 생성", description = "Owner Only")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER')")
    public ResponseEntity<ResCreateStoreDtoV1> createStore(
            @Valid
            @RequestBody ReqCreateStoreDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResCreateStoreDtoV1 response = storeServiceV1.createStore(request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ALL
    @Operation(summary = "스토어 조회", description = "All")
    @GetMapping("/{storeId}")
    public ResponseEntity<ResGetStoreDtoV1> getStore(@PathVariable UUID storeId) {
        ResGetStoreDtoV1 response = storeServiceV1.getStore(storeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스토어 목록조회", description = "All")
    @GetMapping
    public ResponseEntity<PageResponse<ResGetStoreListDtoV1>> getStores(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetStoreListDtoV1> stores = storeServiceV1.getStores(validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(stores));
    }

    // Owner, manager, master
    @Operation(summary = "스토어 수정", description = "Owner, Manager, Master")
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<ResGetStoreDtoV1> updateStore(
            @Valid
            @PathVariable UUID storeId,
            @RequestBody ReqUpdateStoreDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        ResGetStoreDtoV1 response = storeServiceV1.updateStore(storeId, request, authUser);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스토어 숨김 / 노출", description = "Owner, Manager, Master")
    @PatchMapping("/{storeId}/hide")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<Void> hideStore(
            @Valid
            @PathVariable UUID storeId,
            @AuthenticationPrincipal AuthUser authUser) {
        storeServiceV1.hiddenStore(storeId, authUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "스토어 삭제", description = "Owner, Manager, Master")
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public void deleteStore(
            @Valid
            @PathVariable UUID storeId,
            @AuthenticationPrincipal AuthUser authUser) {
        storeServiceV1.deleteStore(storeId, authUser);
    }

    @Operation(summary = "조건별 조회", description = "가계명,카테고라,지역명으로 조회 가능")
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
