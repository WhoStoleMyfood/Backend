package com.example.whostolemyfood.address.presentation.controller;

import com.example.whostolemyfood.address.application.service.AddressServiceV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.user.application.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Address API", description = "배송지 관리 API")
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Validated
public class AddressControllerV1 {

    private final AddressServiceV1 addressService;

    @Operation(summary = "배송지 생성", description = "로그인한 사용자의 새로운 배송지를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<ResCreateAddressDtoV1> createAddress(
            @Valid @RequestBody ReqCreateAddressDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(addressService.createAddress(request, authUser.userId(), authUser.role()));
    }

    @Operation(summary = "본인 배송지 목록 조회", description = "로그인한 사용자의 배송지 목록을 조회합니다. 별칭으로 검색이 가능합니다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<PageResponse<ResGetAddressDtoV1>> getMyAddresses(
            @RequestParam(name = "alias", required = false) String alias,
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);
        Page<ResGetAddressDtoV1> addresses = addressService.getMyAddresses(authUser.userId(), authUser.role(), alias, validatedPageable);
        
        return ResponseEntity.ok(new PageResponse<>(addresses));
    }

    @Operation(summary = "배송지 수정", description = "특정 배송지의 정보를 수정합니다. 본인의 배송지만 수정 가능합니다.")
    @PutMapping("/{addressId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<ResGetAddressDtoV1> updateAddress(
            @PathVariable("addressId") UUID addressId,
            @Valid @RequestBody ReqUpdateAddressDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request, authUser.userId(), authUser.role()));
    }

    @Operation(summary = "배송지 삭제", description = "특정 배송지를 삭제(Soft Delete) 처리합니다. 본인의 배송지만 삭제 가능합니다.")
    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable("addressId") UUID addressId,
            @AuthenticationPrincipal AuthUser authUser) {
        addressService.deleteAddress(addressId, authUser.userId(), authUser.role());
        return ResponseEntity.noContent().build();
    }
}
