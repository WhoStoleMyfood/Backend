package com.example.whostolemyfood.menu.domain.repository;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

    Optional<MenuEntity> findByMenuIdAndStoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID menuId , UUID storeId);
    Optional<MenuEntity> findByMenuIdAndStoreIdAndIsDeletedFalse(UUID menuId , UUID storeId);

    Boolean existsByStoreIdAndNameAndIsDeletedFalse(UUID storeId, String name);

    Page<MenuEntity> findAllByIsHiddenFalseAndIsDeletedFalse(Pageable pageable);
}
