package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
	Page<ReviewEntity> search(ReqGetReviewsDtoV1 condition, Pageable pageable);
}