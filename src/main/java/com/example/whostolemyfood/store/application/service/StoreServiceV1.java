package com.example.whostolemyfood.store.application.service;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRatingSummaryRepository;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreServiceV1 {

    private final StoreRepository storeRepository;
    private final StoreRatingSummaryRepository storeRatingSummaryRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AreaRepository areaRepository;

    // Owner Only
    // 스토어 생성
    @Transactional
    public ResCreateStoreDtoV1 createStore(ReqCreateStoreDtoV1 request, AuthUser authUser) {
        // 유저 권한 확인 로직
        if (authUser.role() != UserRole.OWNER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        UserEntity owner = userRepository.findById(authUser.userId())
                .orElseThrow(()-> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 스토어 이름 중복 확인
        if (storeRepository.existsByNameAndIsDeletedFalse(request.getName())) {
            throw new CustomException(ErrorCode.STORE_DUPLICATION_NAME);
        }

        // 카테고리 존재 확인
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        // 기본적으로 스토어 생성시 평점은 0.0
        StoreRatingSummaryEntity summary = StoreRatingSummaryEntity.builder().build();
        storeRatingSummaryRepository.save(summary);

        AreaEntity area = areaRepository.findByAreaId(request.getAreaId())
                .orElseThrow(()-> new CustomException(ErrorCode.AREA_NOT_FOUND));

        if (!area.getIsActive()) {
            throw new CustomException(ErrorCode.AREA_NOT_ACTIVE);
        }

        StoreEntity store = StoreEntity.builder()
                .user(owner)
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .content(request.getContent())
                .category(category)
                .storeRatingSummary(summary)
                .area(area)
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
    public ResGetStoreDtoV1 getStore(UUID storeId) {
        StoreEntity store = storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        return ResGetStoreDtoV1.from(store);
    }

    // All
    // 스토어 목록 조회
    @Transactional(readOnly = true)
    public Page<ResGetStoreListDtoV1> getStores(Pageable pageable) {
        Page<StoreEntity> stores = storeRepository.findAllByIsHiddenFalseAndIsDeletedFalse(pageable);

        return stores.map(ResGetStoreListDtoV1::from);
    }

    // Owner, Manager, Master
    // 스토어 수정
    @Transactional
    public ResGetStoreDtoV1 updateStore(UUID storeId, ReqUpdateStoreDtoV1 request, AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        // 권한 확인
        validateStoreAccess(store, authUser);

        // 이름 수정시에 발동
        if (!store.getName().equals(request.getName())) {
            if (storeRepository.existsByNameAndIsDeletedFalse(request.getName())) {
                throw new CustomException(ErrorCode.STORE_DUPLICATION_NAME);
            }
        }
        CategoryEntity newCategory = categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                        .orElseThrow(()-> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        store.updateStore(request, newCategory);

        return ResGetStoreDtoV1.from(store);
    }

    // Owner, Manager, Master
    // 스토어 숨김 / 노출
    @Transactional
    public void hiddenStore(UUID storeId, AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        // 권한 확인
        validateStoreAccess(store, authUser);

        store.toggleIsHidden();
    }

    // Owner, Manager, Master
    // 스토어 삭제
    @Transactional
    public void deleteStore(UUID storeId,AuthUser authUser) {
        StoreEntity store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));
        // 권한 확인
        validateStoreAccess(store, authUser);

        store.deleteByOwnerAndMaster(storeId);
    }

    // 권한 확인
    private void validateStoreAccess(StoreEntity store, AuthUser authUser) {
        if (authUser.role() == UserRole.CUSTOMER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (authUser.role() == UserRole.OWNER && !store.getUser().getId().equals(authUser.userId())) {
            throw new CustomException(ErrorCode.STORE_NOT_OWNER);
        }
    }
}
