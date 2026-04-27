package com.example.whostolemyfood.review.infrastructure.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.domain.repository.ReviewRepositoryCustom;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Page<ReviewEntity> search(ReqGetReviewsDtoV1 condition, Pageable pageable) {
		StringBuilder jpql = new StringBuilder(
			"select r from ReviewEntity r " +
				"join fetch r.user u " +
				"join fetch r.store s " +
				"join fetch r.order o " +
				"where r.isDeleted = false "
		);

		StringBuilder countJpql = new StringBuilder(
			"select count(r) from ReviewEntity r " +
				"join r.user u " +
				"join r.store s " +
				"join r.order o " +
				"where r.isDeleted = false "
		);

		List<String> filters = new ArrayList<>();

		if (condition.getStoreId() != null) {
			filters.add("s.id = :storeId");
		}

		if (condition.getRating() != null) {
			filters.add("r.rating = :rating");
		}

		for (String filter : filters) {
			jpql.append(" and ").append(filter);
			countJpql.append(" and ").append(filter);
		}

		jpql.append(" order by r.createdAt desc");

		TypedQuery<ReviewEntity> query = em.createQuery(jpql.toString(), ReviewEntity.class);
		TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

		if (condition.getStoreId() != null) {
			query.setParameter("storeId", condition.getStoreId());
			countQuery.setParameter("storeId", condition.getStoreId());
		}

		if (condition.getRating() != null) {
			query.setParameter("rating", condition.getRating());
			countQuery.setParameter("rating", condition.getRating());
		}

		int page = condition.getPage() == null ? 0 : condition.getPage();
		int size = condition.validatedSize();

		query.setFirstResult(page * size);
		query.setMaxResults(size);

		List<ReviewEntity> content = query.getResultList();
		Long total = countQuery.getSingleResult();

		return new PageImpl<>(content, pageable, total);
	}
}