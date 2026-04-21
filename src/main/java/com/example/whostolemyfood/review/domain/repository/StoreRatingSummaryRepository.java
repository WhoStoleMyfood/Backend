package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.StoreRatingSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRatingSummaryRepository extends JpaRepository<StoreRatingSummaryEntity, UUID> {
}