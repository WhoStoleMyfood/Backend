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
			UUID storeId = store.getId();

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

			StoreRatingSummaryEntity summary = storeRatingSummaryRepository
				.findByStoreIdAndIsDeletedFalse(storeId)
				.orElseGet(() -> StoreRatingSummaryEntity.builder()
					.storeId(storeId)
					.build());

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

			storeRatingSummaryRepository.save(summary);
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
		Integer sum = em.createQuery(
				"select coalesce(sum(r.rating), 0) from ReviewEntity r " +
					"where r.store.id = :storeId and r.isDeleted = false",
				Integer.class
			)
			.setParameter("storeId", storeId)
			.getSingleResult();

		return sum == null ? 0 : sum;
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