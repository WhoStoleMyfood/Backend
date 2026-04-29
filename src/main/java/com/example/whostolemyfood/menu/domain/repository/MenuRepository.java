package com.example.whostolemyfood.menu.domain.repository;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {
    // 활성 메뉴 단건 조회
    Optional<MenuEntity> findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID menuId, UUID storeId);

    // 삭제되지 않은 메뉴 단건 조회
    Optional<MenuEntity> findByMenuIdAndStore_StoreIdAndIsDeletedFalse(UUID menuId, UUID storeId);

    // 활성 메뉴명 중복 확인
    Boolean existsByStore_StoreIdAndNameAndIsDeletedFalse(UUID storeId, String name);

    // 활성 메뉴 목록 조회
    @Query("SELECT m FROM MenuEntity m WHERE m.isHidden = false AND m.isDeleted = false AND m.store.isHidden = false AND m.store.isDeleted = false")
    Page<MenuEntity> findAllByIsHiddenFalseAndIsDeletedFalse(Pageable pageable);

    // 비활성 메뉴 목록 조회
    @Query("SELECT m FROM MenuEntity m WHERE m.store.storeId = :storeId AND (m.isDeleted = true OR m.isHidden = true)")
    Page<MenuEntity> findAllInactiveMenusByStoreId(
            @Param("storeId") UUID storeId, Pageable pageable);
}