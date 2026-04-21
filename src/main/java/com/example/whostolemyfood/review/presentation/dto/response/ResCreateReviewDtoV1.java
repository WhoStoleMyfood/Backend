package com.example.whostolemyfood.review.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ResCreateReviewDtoV1 {
	private UUID reviewId;
	private UUID orderId;
	private UUID storeId;
	private Integer rating;
	private String content;
	private LocalDateTime createdAt;
}