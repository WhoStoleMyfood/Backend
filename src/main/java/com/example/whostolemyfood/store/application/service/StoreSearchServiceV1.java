package com.example.whostolemyfood.store.application.service;

import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.domain.repository.StoreRepositoryCustom;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSearchServiceV1 {

    private final StoreRepositoryCustom storeRepositoryCustom;

    public PageResponse<StoreSearchResponseDtoV1> search(StoreSearchConditionV1 condition, Pageable pageable) {
        // 1. 리포지토리에서 Page 객체 조회
        Page<StoreSearchResponseDtoV1> pageResult = storeRepositoryCustom.searchStore(condition, pageable);

        // 2. 공통 응답 객체인 PageResponse로 변환하여 반환
        return new PageResponse<>(pageResult);
    }
}