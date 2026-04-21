package com.example.whostolemyfood.review.application.service;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.domain.repository.ReviewRepository;
import com.example.whostolemyfood.review.presentation.dto.request.ReqCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqUpdateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public ResCreateReviewDtoV1 createReview(UUID orderId, UUID loginUserId, ReqCreateReviewDtoV1 request) {
		UserEntity loginUser = userRepository.findById(loginUserId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		OrderEntity order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		validateCreatePermission(order, loginUserId);



		// softdelete 처리 분기점 1. 활성 리뷰 있으면 중복 생성 불가
		if (reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(orderId)) {
			throw new IllegalStateException("이미 해당 주문에 대한 리뷰가 존재합니다.");
		}

		// 2. 삭제된 리뷰가 있으면 복구해서 재사용
		ReviewEntity deletedReview = reviewRepository.findByOrder_OrderIdAndIsDeletedTrue(orderId)
			.orElse(null);

		if (deletedReview != null) {
			deletedReview.restoreReview(request.getRating(), request.getContent(), loginUserId);
			return ResCreateReviewDtoV1.builder()
				.reviewId(deletedReview.getReviewId())
				.orderId(deletedReview.getOrder().getOrderId())
				.storeId(deletedReview.getStore().getId())
				.rating(deletedReview.getRating())
				.content(deletedReview.getContent())
				.createdAt(deletedReview.getCreatedAt())
				.build();
		}

		// 3. 아예 없으면 새로 생성
		StoreEntity store = storeRepository.findById(order.getStoreId())
			.orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

		ReviewEntity review = ReviewEntity.builder()
			.order(order)
			.user(loginUser)
			.store(store)
			.rating(request.getRating())
			.content(request.getContent())
			.build();

		review.markCreatedBy(loginUserId);

		ReviewEntity saved = reviewRepository.save(review);

		return ResCreateReviewDtoV1.builder()
			.reviewId(saved.getReviewId())
			.orderId(saved.getOrder().getOrderId())
			.storeId(saved.getStore().getId())
			.rating(saved.getRating())
			.content(saved.getContent())
			.createdAt(saved.getCreatedAt())
			.build();
	}

	public ResGetReviewDtoV1 getReview(UUID reviewId) {
		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

		return toDetailResponse(review);
	}

	@Transactional
	public ResGetReviewDtoV1 updateReview(UUID reviewId, UUID loginUserId, ReqUpdateReviewDtoV1 request) {
		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

		if (!review.getUser().getId().equals(loginUserId)) {
			throw new SecurityException("본인이 작성한 리뷰만 수정할 수 있습니다.");
		}

		review.updateReview(request.getRating(), request.getContent(), loginUserId);

		return toDetailResponse(review);
	}

	@Transactional
	public void deleteReview(UUID reviewId, UUID loginUserId, String authority) {
		ReviewEntity review = reviewRepository.findByReviewIdAndIsDeletedFalse(reviewId)
			.orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

		boolean isWriter = review.getUser().getId().equals(loginUserId);
		boolean isAdmin = "MANAGER".equals(authority) || "MASTER".equals(authority);

		if (!isWriter && !isAdmin) {
			throw new SecurityException("리뷰를 삭제할 권한이 없습니다.");
		}

		review.deleteReview(loginUserId);
	}

	private void validateCreatePermission(OrderEntity order, UUID loginUserId) {
		if (!order.getUserId().equals(loginUserId)) {
			throw new SecurityException("본인 주문에만 리뷰를 작성할 수 있습니다.");
		}

		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new IllegalStateException("COMPLETED 상태의 주문만 리뷰 작성이 가능합니다.");
		}
	}

	private ResGetReviewDtoV1 toDetailResponse(ReviewEntity review) {
		return ResGetReviewDtoV1.builder()
			.reviewId(review.getReviewId())
			.orderId(review.getOrder().getOrderId())
			.storeId(review.getStore().getId())
			.userId(review.getUser().getId())
			.userName(review.getUser().getUserName())
			.rating(review.getRating())
			.content(review.getContent())
			.createdAt(review.getCreatedAt())
			.updatedAt(review.getUpdatedAt())
			.build();
	}
}