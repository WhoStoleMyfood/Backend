//package com.example.whostolemyfood.store.presentation.controller;
//
//import com.example.whostolemyfood.global.util.PageUtil;
//import com.example.whostolemyfood.store.application.StoreSearchServiceV1;
//import com.example.whostolemyfood.store.application.service.StoreServiceV1;
//import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
//import com.example.whostolemyfood.global.response.PageResponse;
//import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//@RequestMapping("/api/v1/search")
//@RequiredArgsConstructor
//public class SearchControllerV1 {
//
//    private final StoreSearchServiceV1 storeSearchService;
//    private final StoreServiceV1 storeServiceV1;
//    @GetMapping
//    public ResponseEntity<PageResponse<ResGetStoreListDtoV1>> getStores(
//            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {
//
//        Pageable validatePageable = PageUtil.validatePageSize(pageable);
//
//        Page<ResGetStoreåListDtoV1> stores = storeServiceV1.getStores(validatePageable);
//        return ResponseEntity.status(HttpStatus.OK).body(new PageResponse<>(stores));
//    }
//
//
//}
