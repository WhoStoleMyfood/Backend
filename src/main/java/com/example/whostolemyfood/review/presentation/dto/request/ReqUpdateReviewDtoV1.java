package com.example.whostolemyfood.review.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "리뷰수정 요청 객체")
public class ReqUpdateReviewDtoV1 {

	@Schema(description = "평점", example = "5")
	@Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 5점 이하여야 합니다.")
	private Integer rating;

	@Schema(description = "리뷰 내용", example = "맛있습니다.")
	@NotBlank(message = "리뷰 내용은 비어 있을 수 없습니다.")
	@Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
	private String content;
}