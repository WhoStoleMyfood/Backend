package com.example.whostolemyfood.review.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ResGetReviewPageDtoV1 {
	private UUID reviewId;
	private UUID orderId;
	private UUID storeId;
	private UUID userId;
	private String userName;
	private Integer rating;
	private String content;
	private LocalDateTime createdAt;
}