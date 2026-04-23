package com.example.whostolemyfood.address.presentation.controller;

import com.example.whostolemyfood.address.application.service.AddressServiceV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.util.PageUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Validated
public class AddressControllerV1 {

    private final AddressServiceV1 addressService;

    /**
     * 배송지 생성 API
     */
    @PostMapping
    public ResponseEntity<ResCreateAddressDtoV1> createAddress(
            @Valid @RequestBody ReqCreateAddressDtoV1 request) {
        // TODO: [인증/인가] SecurityContext 기반 처리는 서비스 레이어 TODO 확인
        return ResponseEntity.ok(addressService.createAddress(request));
    }

    /**
     * 본인 배송지 목록 조회 및 검색 API
     */
    @GetMapping
    public ResponseEntity<Page<ResGetAddressDtoV1>> getMyAddresses(
            @RequestParam(name = "alias", required = false) String alias,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);
        
        return ResponseEntity.ok(addressService.getMyAddresses(alias, validatedPageable));
    }

    /**
     * 배송지 수정 API
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ResGetAddressDtoV1> updateAddress(
            @PathVariable("addressId") UUID addressId,
            @Valid @RequestBody ReqUpdateAddressDtoV1 request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    /**
     * 배송지 삭제 API (Soft Delete)
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("addressId") UUID addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }
}
