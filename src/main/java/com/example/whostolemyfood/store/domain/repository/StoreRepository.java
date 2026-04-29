package com.example.whostolemyfood.store.domain.repository;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {
    // 활성 가게명 중복 확인
    Boolean existsByNameAndIsDeletedFalse(String name);

    // 활성 가게 단건 조회
    Optional<StoreEntity> findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID storeId);
    // 삭제되지 않은 가게 단건 조회
    Optional<StoreEntity> findByStoreIdAndIsDeletedFalse(UUID storeId);

    // 활성 가게 목록 조회
    Page<StoreEntity> findAllByIsHiddenFalseAndIsDeletedFalse(Pageable pageable);

    // 비활성 가게 목록 조회
    @Query("SELECT s FROM StoreEntity s WHERE s.isDeleted = true OR s.isHidden = true")
    Page<StoreEntity> findAllInactiveStores(Pageable pageable);

    // 오너의 비활성 가게 목록 조회
    @Query("SELECT s FROM StoreEntity s WHERE s.user.id = :userId AND (s.isDeleted = true OR s.isHidden = true)")
    Page<StoreEntity> findInactiveStoresByUserId(@Param("userId") UUID userId, Pageable pageable);
}