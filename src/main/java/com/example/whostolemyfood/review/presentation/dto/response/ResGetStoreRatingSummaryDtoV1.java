package com.example.whostolemyfood.review.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ResGetStoreRatingSummaryDtoV1 {

	private UUID storeId;
	private Integer reviewCount;
	private Integer totalRatingSum;
	private BigDecimal averageRating;
	private Integer rating1Count;
	private Integer rating2Count;
	private Integer rating3Count;
	private Integer rating4Count;
	private Integer rating5Count;
}