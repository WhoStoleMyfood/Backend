package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, ReviewRepositoryCustom {

	Optional<ReviewEntity> findByReviewIdAndIsDeletedFalse(UUID reviewId);

	boolean existsByOrder_OrderIdAndIsDeletedFalse(UUID orderId);

	//is_deleted 확인(재작성 로직)
	Optional<ReviewEntity> findByOrder_OrderIdAndIsDeletedTrue(UUID orderId);

	@EntityGraph(attributePaths = {"user", "store", "order"})
	Page<ReviewEntity> findAllByIsDeletedFalse(Pageable pageable);
}