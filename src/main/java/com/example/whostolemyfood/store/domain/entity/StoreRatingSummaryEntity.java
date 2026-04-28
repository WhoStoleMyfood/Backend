package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "p_store_rating_summarys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreRatingSummaryEntity extends BaseSoftDeleteEntity {

	@Id
	@Column(name = "store_rating_id", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "review_count", nullable = false)
	@Builder.Default
	private Integer reviewCount = 0;

	@Column(name = "total_rating_sum", nullable = false)
	@Builder.Default
	private Integer totalRatingSum = 0;

	@Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
	@Builder.Default
	private BigDecimal averageRating = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);

	@Column(name = "rating_1_count", nullable = false)
	@Builder.Default
	private Integer rating1Count = 0;

	@Column(name = "rating_2_count", nullable = false)
	@Builder.Default
	private Integer rating2Count = 0;

	@Column(name = "rating_3_count", nullable = false)
	@Builder.Default
	private Integer rating3Count = 0;

	@Column(name = "rating_4_count", nullable = false)
	@Builder.Default
	private Integer rating4Count = 0;

	@Column(name = "rating_5_count", nullable = false)
	@Builder.Default
	private Integer rating5Count = 0;

	public static StoreRatingSummaryEntity createDefault() {
		return StoreRatingSummaryEntity.builder()
			.reviewCount(0)
			.totalRatingSum(0)
			.averageRating(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP))
			.rating1Count(0)
			.rating2Count(0)
			.rating3Count(0)
			.rating4Count(0)
			.rating5Count(0)
			.build();
	}

	public void refresh(
		int reviewCount,
		int totalRatingSum,
		BigDecimal averageRating,
		int rating1Count,
		int rating2Count,
		int rating3Count,
		int rating4Count,
		int rating5Count
	) {
		this.reviewCount = reviewCount;
		this.totalRatingSum = totalRatingSum;
		this.averageRating = averageRating;
		this.rating1Count = rating1Count;
		this.rating2Count = rating2Count;
		this.rating3Count = rating3Count;
		this.rating4Count = rating4Count;
		this.rating5Count = rating5Count;
	}

	public static StoreRatingSummaryEntity createDefault() {
		return StoreRatingSummaryEntity.builder()
				.reviewCount(0)
				.totalRatingSum(0)
				.averageRating(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP))
				.rating1Count(0)
				.rating2Count(0)
				.rating3Count(0)
				.rating4Count(0)
				.rating5Count(0)
				.isDeleted(false)
				.build();
	}
}