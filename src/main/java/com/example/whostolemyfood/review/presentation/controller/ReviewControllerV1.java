package com.example.whostolemyfood.review.presentation.controller;

import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.review.application.service.ReviewRatingBatchService;
import com.example.whostolemyfood.review.application.service.ReviewServiceV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqUpdateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewPageDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetStoreRatingSummaryDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Review API", description = "리뷰 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewControllerV1 {

	private final ReviewServiceV1 reviewServiceV1;
	private final ReviewRatingBatchService reviewRatingBatchService;

	@Operation(summary = "리뷰 작성", description = "[CUSTOMER] 리뷰를 작성합니다.")
	@PostMapping("/orders/{orderId}/reviews")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ResCreateReviewDtoV1> createReview(
		@PathVariable UUID orderId,
		@Valid @RequestBody ReqCreateReviewDtoV1 request,
		@AuthenticationPrincipal AuthUser loginUser
	) {
		ResCreateReviewDtoV1 response = reviewServiceV1.createReview(
			orderId,
			loginUser.userId(),
			loginUser.role().name(),
			request
		);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "특정 리뷰 상세 조회", description = "[ALL] 특정 리뷰를 조회합니다.")
	@GetMapping("/reviews/{reviewId}")
	public ResponseEntity<ResGetReviewDtoV1> getReview(
		@PathVariable UUID reviewId
	) {
		ResGetReviewDtoV1 response = reviewServiceV1.getReview(reviewId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "특정 리뷰 수정", description = "[CUSTOMER] 작성한 특정 리뷰를 수정합니다.")
	@PutMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ResGetReviewDtoV1> updateReview(
		@PathVariable UUID reviewId,
		@Valid @RequestBody ReqUpdateReviewDtoV1 request,
		@AuthenticationPrincipal AuthUser loginUser
	) {
		ResGetReviewDtoV1 response = reviewServiceV1.updateReview(
			reviewId,
			loginUser.userId(),
			loginUser.role().name(),
			request
		);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "리뷰 삭제", description = "[CUSTOMER / MANAGER / MASTER] 특정 리뷰를 삭제합니다.")
	@DeleteMapping("/reviews/{reviewId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','MANAGER','MASTER')")
	public ResponseEntity<String> deleteReview(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal AuthUser loginUser
	) {
		reviewServiceV1.deleteReview(
			reviewId,
			loginUser.userId(),
			loginUser.role().name()
		);
		return ResponseEntity.ok("리뷰 삭제가 완료되었습니다.");
	}

	@Operation(summary = "평점 조회", description = "[ALL] 가게 리뷰 평점을 조회 합니다.")
	@GetMapping("/stores/{storeId}/rating-summary")
	public ResponseEntity<ResGetStoreRatingSummaryDtoV1> getStoreRatingSummary(
		@PathVariable UUID storeId
	) {
		ResGetStoreRatingSummaryDtoV1 response = reviewServiceV1.getStoreRatingSummary(storeId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "리뷰 목록 조회", description = "[ALL] 리뷰 목록을 조회합니다.")
	@GetMapping("/reviews")
	public ResponseEntity<PageResponse<ResGetReviewPageDtoV1>> getReviews(
		@ModelAttribute ReqGetReviewsDtoV1 request
	) {
		PageResponse<ResGetReviewPageDtoV1> response =
			new PageResponse<>(reviewServiceV1.getReviews(request));

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "가게 평점 집계", description = "[MANAGER / MASTER] 가게 평점을 집계합니다.")
	@PostMapping("/reviews/rating-summary/refresh")
	@PreAuthorize("hasAnyRole('MANAGER','MASTER')")
	public ResponseEntity<String> refreshRatingSummary() {
		reviewRatingBatchService.refreshAllStoreRatings();
		return ResponseEntity.ok("가게 평점 집계가 완료되었습니다.");
	}
}