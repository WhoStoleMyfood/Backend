package com.example.whostolemyfood.store.application.service;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreServiceV1 {

    private final StoreRepository storeRepository;

    // Owner Only
    // 스토어 생성
    @Transactional
    public ResCreateStoreDtoV1 createStore(ReqCreateStoreDtoV1 request) {
        // 유저 권한 확인 로직

        // 스토어 이름 중복 확인
        if (storeRepository.existsByName(request.getStoreName())) {
            throw new IllegalArgumentException("이미 존재하는 스토어 이름입니다 : " + request.getStoreName());
        }

        StoreEntity store = StoreEntity.builder()
                .name(request.getStoreName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .content(request.getContent())
                .minOrderPrice(request.getMinOrderPrice())
                .status(StoreStatus.OPEN)
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();

        StoreEntity savedStore = storeRepository.save(store);
        return ResCreateStoreDtoV1.from(savedStore);
    }

    // All
    // 스토어 단건 조회
    @Transactional(readOnly = true)
    public ResGetStoreDtoV1 getStore(UUID id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어가 존재하지 않습니다"));

        return ResGetStoreDtoV1.from(store);
    }

    // 스토어 목록 조회
    @Transactional(readOnly = true)
    public Page<ResGetStoreListDtoV1> getStores(Pageable pageable) {
        Page<StoreEntity> stores = storeRepository.findAll(pageable);

        return stores.map(ResGetStoreListDtoV1::from);
    }

    // owner, manager, master
    // 스토어 수정
    @Transactional
    public ResGetStoreDtoV1 updateStore(UUID id, ReqUpdateStoreDtoV1 request) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어가 존재하지 않습니다"));
        // 유저 권한 확인
        // owner일시
        store.updateAllFields(request);

        storeRepository.save(store);
        return ResGetStoreDtoV1.from(store);
    }

    // 스토어 삭제
    @Transactional
    public void deleteStore(UUID id) {
        // 유저 권한 확인
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어가 존재하지 않습니다"));
        store.deleteByOwnerAndMaster(id);
    }


}
