package com.example.whostolemyfood.store.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRatingSummaryRepository extends JpaRepository<StoreRatingSummaryEntity, UUID> {

	Optional<StoreRatingSummaryEntity> findByStoreIdAndIsDeletedFalse(UUID storeId);

}