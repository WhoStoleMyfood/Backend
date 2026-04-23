package com.example.whostolemyfood.store.domain.repository;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<StoreEntity, UUID> {

    Boolean existsByNameAndIsDeletedFalse(String name);

    Optional<StoreEntity> findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(UUID storeId);
    Optional<StoreEntity> findByStoreIdAndIsDeletedFalse(UUID storeId);

    Page<StoreEntity> findAllByIsHiddenFalseAndIsDeletedFalse(Pageable pageable);
}