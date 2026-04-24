package com.example.whostolemyfood.menu.domain.repository;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {
    // 조회용
    Optional<MenuEntity> findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID menuId , UUID storeId);
    // 수정 / 노출 / 삭제용
    Optional<MenuEntity> findByMenuIdAndStore_StoreIdAndIsDeletedFalse(UUID menuId , UUID storeId);

    // 이름 중복 확인
    Boolean existsByStore_StoreIdAndNameAndIsDeletedFalse(UUID storeId, String name);

    // 목록 조회용
    Page<MenuEntity> findAllByIsHiddenFalseAndIsDeletedFalse(Pageable pageable);
}
