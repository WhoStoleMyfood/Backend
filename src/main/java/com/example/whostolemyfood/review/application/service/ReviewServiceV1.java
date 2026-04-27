package com.example.whostolemyfood.review.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.domain.repository.ReviewRepository;
import com.example.whostolemyfood.review.presentation.dto.request.ReqCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqUpdateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewPageDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetStoreRatingSummaryDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceV1 {

	private final ReviewRepository reviewRepository;
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public ResCreateReviewDtoV1 createReview(
		UUID orderId,
		UUID loginUserId,
		String tokenRole,
		ReqCreateReviewDtoV1 request
	) {
		UserEntity loginUser = validateActiveUserAndRole(loginUserId, tokenRole);

		if (loginUser.getUserRole() != UserRole.CUSTOMER) {
			throw new CustomException(ErrorCode.REVIEW_ONLY_CUSTOMER);
		}

		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		validateOrderOwner(order, loginUserId);
		validateOrderCompleted(order);

		if (reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(orderId)) {
			throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
		}

		StoreEntity store = storeRepository.findById(order.getStoreId())
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		ReviewEntity deletedReview = reviewRepository.findByOrder_OrderIdAndIsDeletedTrue(orderId)
			.orElse(null);

		if (deletedReview != null) {
			deletedReview.restoreReview(
				request.getRating(),
				request.getContent(),
				loginUserId
			);
			return toCreateResponse(deletedReview);
		}

		ReviewEntity review = ReviewEntity.builder()
			.order(order)
			.user(loginUser)
			.store(store)
			.rating(request.getRating())
			.content(request.getContent())
			.build();

		review.markCreatedBy(loginUserId);

		ReviewEntity saved = reviewRepository.save(review);
		return toCreateResponse(saved);
	}

	public ResGetReviewDtoV1 getReview(UUID reviewId) {
		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		return toGetResponse(review);
	}

	@Transactional
	public ResGetReviewDtoV1 updateReview(
		UUID reviewId,
		UUID loginUserId,
		String tokenRole,
		ReqUpdateReviewDtoV1 request
	) {
		UserEntity loginUser = validateActiveUserAndRole(loginUserId, tokenRole);

		if (loginUser.getUserRole() != UserRole.CUSTOMER) {
			throw new CustomException(ErrorCode.REVIEW_ONLY_CUSTOMER);
		}

		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		if (!review.getUser().getId().equals(loginUserId)) {
			throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
		}

		review.updateReview(
			request.getRating(),
			request.getContent(),
			loginUserId
		);

		return toGetResponse(review);
	}

	@Transactional
	public void deleteReview(
		UUID reviewId,
		UUID loginUserId,
		String tokenRole
	) {
		UserEntity loginUser = validateActiveUserAndRole(loginUserId, tokenRole);

		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));

		UserRole dbRole = loginUser.getUserRole();

		if (dbRole == UserRole.CUSTOMER) {
			if (!review.getUser().getId().equals(loginUserId)) {
				throw new CustomException(ErrorCode.REVIEW_NOT_OWNER);
			}
		} else if (dbRole != UserRole.MANAGER && dbRole != UserRole.MASTER) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		review.deleteReview(loginUserId);
	}

	public Page<ResGetReviewPageDtoV1> getReviews(ReqGetReviewsDtoV1 request) {
		Pageable pageable = request.toPageable();

		return reviewRepository.search(request, pageable)
			.map(this::toPageResponse);
	}

	public ResGetStoreRatingSummaryDtoV1 getStoreRatingSummary(UUID storeId) {
		StoreEntity store = storeRepository.findById(storeId)
			.orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

		StoreRatingSummaryEntity summary = store.getStoreRatingSummary();

		//Summary 없을 때 0점으로 반환
		if (summary == null || Boolean.TRUE.equals(summary.getIsDeleted())) {
			return ResGetStoreRatingSummaryDtoV1.builder()
				.storeId(storeId)
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

		return ResGetStoreRatingSummaryDtoV1.builder()
			.storeId(storeId)
			.reviewCount(summary.getReviewCount())
			.totalRatingSum(summary.getTotalRatingSum())
			.averageRating(summary.getAverageRating())
			.rating1Count(summary.getRating1Count())
			.rating2Count(summary.getRating2Count())
			.rating3Count(summary.getRating3Count())
			.rating4Count(summary.getRating4Count())
			.rating5Count(summary.getRating5Count())
			.build();
	}

	private UserEntity validateActiveUserAndRole(UUID loginUserId, String tokenRole) {
		UserEntity user = userRepository.findById(loginUserId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (Boolean.TRUE.equals(user.getIsDeleted())) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		String dbRole = user.getUserRole().name();
		if (!dbRole.equals(tokenRole)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}

		return user;
	}

	private void validateOrderOwner(OrderEntity order, UUID loginUserId) {
		if (!order.getUserId().equals(loginUserId)) {
			throw new CustomException(ErrorCode.ORDER_NOT_OWNER);
		}
	}

	private void validateOrderCompleted(OrderEntity order) {
		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new CustomException(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
		}
	}

	private ResCreateReviewDtoV1 toCreateResponse(ReviewEntity review) {
		return ResCreateReviewDtoV1.builder()
			.reviewId(review.getReviewId())
			.orderId(review.getOrder().getOrderId())
			.storeId(review.getStore().getStoreId())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.build();
	}

	private ResGetReviewDtoV1 toGetResponse(ReviewEntity review) {
		return ResGetReviewDtoV1.builder()
			.reviewId(review.getReviewId())
			.orderId(review.getOrder().getOrderId())
			.storeId(review.getStore().getStoreId())
			.userId(review.getUser().getId())
			.userName(review.getUser().getUserName())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.updatedAt(review.getUpdatedAt())
			.build();
	}

	private ResGetReviewPageDtoV1 toPageResponse(ReviewEntity review) {
		return ResGetReviewPageDtoV1.builder()
			.reviewId(review.getReviewId())
			.orderId(review.getOrder().getOrderId())
			.storeId(review.getStore().getStoreId())
			.userId(review.getUser().getId())
			.userName(review.getUser().getUserName())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.build();
	}
}