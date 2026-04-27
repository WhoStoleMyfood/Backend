package com.example.whostolemyfood.store.domain.repository;

import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StoreRepositoryCustom {

    Page<StoreSearchResponseDtoV1> searchStore(StoreSearchConditionV1 cond, Pageable pageable);
}
