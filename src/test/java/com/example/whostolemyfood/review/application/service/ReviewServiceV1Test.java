package com.example.whostolemyfood.review.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.domain.repository.ReviewRepository;
import com.example.whostolemyfood.review.presentation.dto.request.ReqCreateReviewDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceV1Test {

	@InjectMocks
	private ReviewServiceV1 reviewService;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private StoreRepository storeRepository;

	@Test
	void 리뷰_생성_성공() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();

		UserEntity user = mock(UserEntity.class);
		OrderEntity order = mock(OrderEntity.class);
		StoreEntity store = mock(StoreEntity.class);

		ReqCreateReviewDtoV1 request = createRequest(5, "맛있어요");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(user.getIsDeleted()).thenReturn(false);
		when(user.getUserRole()).thenReturn(UserRole.CUSTOMER);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(order.getUserId()).thenReturn(userId);
		when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);
		when(order.getStoreId()).thenReturn(storeId);

		when(reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(orderId)).thenReturn(false);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reviewRepository.findByOrder_OrderIdAndIsDeletedTrue(orderId)).thenReturn(Optional.empty());

		when(order.getOrderId()).thenReturn(orderId);
		when(store.getStoreId()).thenReturn(storeId);

		when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> {
			ReviewEntity review = invocation.getArgument(0);
			ReflectionTestUtils.setField(review, "reviewId", reviewId);
			return review;
		});

		var result = reviewService.createReview(orderId, userId, "CUSTOMER", request);

		assertThat(result.getReviewId()).isEqualTo(reviewId);
		assertThat(result.getRating()).isEqualTo(5);
		assertThat(result.getContent()).isEqualTo("맛있어요");
	}

	@Test
	void 리뷰_생성_실패_중복리뷰() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();

		UserEntity user = mock(UserEntity.class);
		OrderEntity order = mock(OrderEntity.class);

		ReqCreateReviewDtoV1 request = createRequest(5, "중복");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(user.getIsDeleted()).thenReturn(false);
		when(user.getUserRole()).thenReturn(UserRole.CUSTOMER);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(order.getUserId()).thenReturn(userId);
		when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);

		when(reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(orderId)).thenReturn(true);

		assertThatThrownBy(() ->
			reviewService.createReview(orderId, userId, "CUSTOMER", request)
		)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS);
	}

	@Test
	void 리뷰_생성_실패_주문완료상태아님() {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();

		UserEntity user = mock(UserEntity.class);
		OrderEntity order = mock(OrderEntity.class);

		ReqCreateReviewDtoV1 request = createRequest(5, "상태 실패");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(user.getIsDeleted()).thenReturn(false);
		when(user.getUserRole()).thenReturn(UserRole.CUSTOMER);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(order.getUserId()).thenReturn(userId);
		when(order.getStatus()).thenReturn(OrderStatus.PENDING);

		assertThatThrownBy(() ->
			reviewService.createReview(orderId, userId, "CUSTOMER", request)
		)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.REVIEW_ORDER_NOT_COMPLETED);
	}

	@Test
	void 평점요약_조회_성공() {
		UUID storeId = UUID.randomUUID();

		StoreEntity store = mock(StoreEntity.class);
		StoreRatingSummaryEntity summary = StoreRatingSummaryEntity.createDefault();
		summary.refresh(2, 8, new BigDecimal("4.0"), 0, 0, 1, 0, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(store.getStoreRatingSummary()).thenReturn(summary);

		var result = reviewService.getStoreRatingSummary(storeId);

		assertThat(result.getStoreId()).isEqualTo(storeId);
		assertThat(result.getReviewCount()).isEqualTo(2);
		assertThat(result.getTotalRatingSum()).isEqualTo(8);
		assertThat(result.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.0"));
	}

	@Test
	void 평점요약_조회_가게없음() {
		UUID storeId = UUID.randomUUID();

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		assertThatThrownBy(() ->
			reviewService.getStoreRatingSummary(storeId)
		)
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.STORE_NOT_FOUND);
	}

	private ReqCreateReviewDtoV1 createRequest(Integer rating, String content) {
		ReqCreateReviewDtoV1 request = new ReqCreateReviewDtoV1();
		ReflectionTestUtils.setField(request, "rating", rating);
		ReflectionTestUtils.setField(request, "content", content);
		return request;
	}
}