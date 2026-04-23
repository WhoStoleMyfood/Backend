package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import org.springframework.data.domain.Page;

public interface ReviewRepositoryCustom {
	Page<ReviewEntity> search(ReqGetReviewsDtoV1 condition);
}