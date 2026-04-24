package com.example.whostolemyfood.review.application.service;

import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRatingSummaryRepository;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewRatingBatchService {

	private final StoreRepository storeRepository;
	private final StoreRatingSummaryRepository storeRatingSummaryRepository;

	@PersistenceContext
	private EntityManager em;

	public void refreshAllStoreRatings() {
		List<StoreEntity> stores = storeRepository.findAll();

		for (StoreEntity store : stores) {
			UUID storeId = store.getStoreId();

			int reviewCount = getReviewCount(storeId);
			int totalRatingSum = getTotalRatingSum(storeId);

			int rating1Count = getCountByRating(storeId, 1);
			int rating2Count = getCountByRating(storeId, 2);
			int rating3Count = getCountByRating(storeId, 3);
			int rating4Count = getCountByRating(storeId, 4);
			int rating5Count = getCountByRating(storeId, 5);

			BigDecimal averageRating = reviewCount == 0
				? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
				: BigDecimal.valueOf((double) totalRatingSum / reviewCount)
				.setScale(1, RoundingMode.HALF_UP);

			StoreRatingSummaryEntity summary = null;

			if (store.getStoreRatingId() != null) {
				summary = storeRatingSummaryRepository
					.findByIdAndIsDeletedFalse(store.getStoreRatingId())
					.orElse(null);
			}

			if (summary == null) {
				summary = StoreRatingSummaryEntity.builder()
					.reviewCount(reviewCount)
					.totalRatingSum(totalRatingSum)
					.averageRating(averageRating)
					.rating1Count(rating1Count)
					.rating2Count(rating2Count)
					.rating3Count(rating3Count)
					.rating4Count(rating4Count)
					.rating5Count(rating5Count)
					.build();

				StoreRatingSummaryEntity savedSummary = storeRatingSummaryRepository.save(summary);

				store.updateStoreRatingId(savedSummary.getId());
				storeRepository.save(store);
			} else {
				summary.refresh(
					reviewCount,
					totalRatingSum,
					averageRating,
					rating1Count,
					rating2Count,
					rating3Count,
					rating4Count,
					rating5Count
				);
			}
		}
	}

	private int getReviewCount(UUID storeId) {
		Long count = em.createQuery(
				"select count(r) from ReviewEntity r " +
					"where r.store.id = :storeId and r.isDeleted = false",
				Long.class
			)
			.setParameter("storeId", storeId)
			.getSingleResult();

		return count.intValue();
	}

	private int getTotalRatingSum(UUID storeId) {
		Long sum = em.createQuery(
				"select coalesce(sum(r.rating), 0) from ReviewEntity r " +
					"where r.store.id = :storeId and r.isDeleted = false",
				Long.class
			)
			.setParameter("storeId", storeId)
			.getSingleResult();

		return sum == null ? 0 : sum.intValue();
	}

	private int getCountByRating(UUID storeId, int rating) {
		Long count = em.createQuery(
				"select count(r) from ReviewEntity r " +
					"where r.store.id = :storeId and r.rating = :rating and r.isDeleted = false",
				Long.class
			)
			.setParameter("storeId", storeId)
			.setParameter("rating", rating)
			.getSingleResult();

		return count.intValue();
	}
}