package com.example.whostolemyfood.review.presentation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.UUID;

@Getter
@Setter
public class ReqGetReviewsDtoV1 {

	private UUID storeId;

	@Min(value = 1, message = "평점은 1 이상이어야 합니다.")
	@Max(value = 5, message = "평점은 5 이하여야 합니다.")
	private Integer rating;

	@Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
	private Integer page = 0;

	private Integer size = 10;

	private String sort = "createdAt,DESC";

	public int validatedSize() {
		if (size == null) {
			return 10;
		}
		return (size == 10 || size == 30 || size == 50) ? size : 10;
	}

	public Sort toSort() {
		if (sort == null || sort.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "createdAt");
		}

		String[] split = sort.split(",");
		String property = split[0];
		Sort.Direction direction = split.length > 1
			? Sort.Direction.fromString(split[1])
			: Sort.Direction.DESC;

		return Sort.by(direction, property);
	}
}