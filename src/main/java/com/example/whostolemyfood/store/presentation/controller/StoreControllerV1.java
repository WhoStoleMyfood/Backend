package com.example.whostolemyfood.store.presentation.controller;

import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/store")
@RequiredArgsConstructor
public class StoreControllerV1 {

    private final StoreServiceV1  storeServiceV1;

    // Owner Only
    @Operation(summary = "스토어 생성")
    @PostMapping
    public ResponseEntity<ResCreateStoreDtoV1> createStore(@RequestBody ReqCreateStoreDtoV1 request) {
        ResCreateStoreDtoV1 response = storeServiceV1.createStore(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ALL
    @Operation(summary = "스토어 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ResGetStoreDtoV1> getStore(@PathVariable UUID id) {
        ResGetStoreDtoV1 response = storeServiceV1.getStore(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스토어 목록조회")
    @GetMapping
    public ResponseEntity<PageResponse<ResGetStoreListDtoV1>> getStores(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        Pageable validatePageable = PageUtil.validatePageSize(pageable);

        Page<ResGetStoreListDtoV1> stores = storeServiceV1.getStores(validatePageable);
        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(stores));
    }

    // Owner, manager, master
    @Operation(summary = "스토어 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ResGetStoreDtoV1> updateStore(@PathVariable UUID id, @RequestBody ReqUpdateStoreDtoV1 request) {
        ResGetStoreDtoV1 response = storeServiceV1.updateStore(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "스토어 삭제")
    @DeleteMapping("/{id}")
    public void deleteStore(@PathVariable UUID id) {
        storeServiceV1.deleteStore(id);
    }
}
