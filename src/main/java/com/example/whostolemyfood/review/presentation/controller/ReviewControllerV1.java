package com.example.whostolemyfood.review.presentation.controller;

import com.example.whostolemyfood.review.application.service.ReviewServiceV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.request.ReqUpdateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewDtoV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewControllerV1 {

	private final ReviewServiceV1 reviewServiceV1;

	@PostMapping("/orders/{orderId}/reviews")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ResCreateReviewDtoV1> createReview(
		@PathVariable UUID orderId,
		@Valid @RequestBody ReqCreateReviewDtoV1 request,
		@AuthenticationPrincipal UserEntity loginUser
	) {
		ResCreateReviewDtoV1 response = reviewServiceV1.createReview(
			orderId,
			loginUser.getUserId(),
			request
		);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/reviews/{reviewId}")
	public ResponseEntity<ResGetReviewDtoV1> getReview(
		@PathVariable UUID reviewId
	) {
		ResGetReviewDtoV1 response = reviewServiceV1.getReview(reviewId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ResponseEntity<ResGetReviewDtoV1> updateReview(
		@PathVariable UUID reviewId,
		@Valid @RequestBody ReqUpdateReviewDtoV1 request,
		@AuthenticationPrincipal UserEntity loginUser
	) {
		ResGetReviewDtoV1 response = reviewServiceV1.updateReview(
			reviewId,
			loginUser.getUserId(),
			request
		);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/reviews/{reviewId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','MANAGER','MASTER')")
	public ResponseEntity<Void> deleteReview(
		@PathVariable UUID reviewId,
		@AuthenticationPrincipal UserEntity loginUser
	) {
		reviewServiceV1.deleteReview(
			reviewId,
			loginUser.getUserId(),
			loginUser.getRole().name()
		);
		return ResponseEntity.noContent().build();
	}
}