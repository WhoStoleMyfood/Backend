package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

	Optional<ReviewEntity> findByReviewIdAndIsDeletedFalse(UUID reviewId);

	boolean existsByOrder_OrderIdAndIsDeletedFalse(UUID orderId);
}