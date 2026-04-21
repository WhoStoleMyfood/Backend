package com.example.whostolemyfood.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ReqCreateReviewDtoV1 {

	@Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 5점 이하여야 합니다.")
	private Integer rating;

	@NotBlank(message = "리뷰 내용은 비어 있을 수 없습니다.")
	@Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
	private String content;
}