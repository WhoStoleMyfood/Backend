package com.example.whostolemyfood.review.persentation.controller;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtAuthenticationFilter;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.review.application.service.ReviewRatingBatchService;
import com.example.whostolemyfood.review.application.service.ReviewServiceV1;
import com.example.whostolemyfood.review.presentation.controller.ReviewControllerV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResCreateReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetReviewPageDtoV1;
import com.example.whostolemyfood.review.presentation.dto.response.ResGetStoreRatingSummaryDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = ReviewControllerV1.class,
	excludeFilters = {
		@ComponentScan.Filter(
			type = FilterType.ASSIGNABLE_TYPE,
			classes = SecurityConfig.class
		)
	}
)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerV1Test {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReviewServiceV1 reviewServiceV1;

	@MockitoBean
	private ReviewRatingBatchService reviewRatingBatchService;

	@MockitoBean
	private JwtUtil jwtUtil;

	@MockitoBean
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Test
	void 리뷰_생성_성공() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();

		ResCreateReviewDtoV1 response = ResCreateReviewDtoV1.builder()
			.reviewId(reviewId)
			.orderId(orderId)
			.storeId(storeId)
			.rating(5)
			.content("맛있어요")
			.createdAt(LocalDateTime.now())
			.build();

		when(reviewServiceV1.createReview(eq(orderId), eq(userId), eq("CUSTOMER"), any()))
			.thenReturn(response);

		mockMvc.perform(post("/api/v1/orders/{orderId}/reviews", orderId)
				.with(loginUser(userId, UserRole.CUSTOMER))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
                                {
                                  "rating": 5,
                                  "content": "맛있어요"
                                }
                                """))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
			.andExpect(jsonPath("$.orderId").value(orderId.toString()))
			.andExpect(jsonPath("$.storeId").value(storeId.toString()))
			.andExpect(jsonPath("$.rating").value(5))
			.andExpect(jsonPath("$.content").value("맛있어요"));
	}

	@Test
	void 리뷰_단건조회_성공() throws Exception {
		UUID reviewId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		ResGetReviewDtoV1 response = ResGetReviewDtoV1.builder()
			.reviewId(reviewId)
			.orderId(orderId)
			.storeId(storeId)
			.userId(userId)
			.userName("고객A")
			.rating(5)
			.content("리뷰 조회")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		when(reviewServiceV1.getReview(reviewId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/reviews/{reviewId}", reviewId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
			.andExpect(jsonPath("$.rating").value(5))
			.andExpect(jsonPath("$.content").value("리뷰 조회"));
	}

	@Test
	void 리뷰_수정_성공() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();

		ResGetReviewDtoV1 response = ResGetReviewDtoV1.builder()
			.reviewId(reviewId)
			.orderId(orderId)
			.storeId(storeId)
			.userId(userId)
			.userName("고객A")
			.rating(4)
			.content("수정된 리뷰")
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.build();

		when(reviewServiceV1.updateReview(eq(reviewId), eq(userId), eq("CUSTOMER"), any()))
			.thenReturn(response);

		mockMvc.perform(put("/api/v1/reviews/{reviewId}", reviewId)
				.with(loginUser(userId, UserRole.CUSTOMER))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
                                {
                                  "rating": 4,
                                  "content": "수정된 리뷰"
                                }
                                """))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
			.andExpect(jsonPath("$.rating").value(4))
			.andExpect(jsonPath("$.content").value("수정된 리뷰"));
	}

	@Test
	void 리뷰_삭제_성공() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();

		mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId)
				.with(loginUser(userId, UserRole.CUSTOMER)))
			.andExpect(status().isOk())
			.andExpect(content().string("리뷰 삭제가 완료되었습니다."));

		verify(reviewServiceV1).deleteReview(reviewId, userId, "CUSTOMER");
	}

	@Test
	void 리뷰_목록조회_성공() throws Exception {
		UUID reviewId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		ResGetReviewPageDtoV1 review = ResGetReviewPageDtoV1.builder()
			.reviewId(reviewId)
			.orderId(orderId)
			.storeId(storeId)
			.userId(userId)
			.userName("고객A")
			.rating(5)
			.content("목록 리뷰")
			.createdAt(LocalDateTime.now())
			.build();

		when(reviewServiceV1.getReviews(any()))
			.thenReturn(new PageImpl<>(List.of(review)));

		mockMvc.perform(get("/api/v1/reviews")
				.param("storeId", storeId.toString())
				.param("rating", "5")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "createdAt,DESC"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].reviewId").value(reviewId.toString()))
			.andExpect(jsonPath("$.content[0].rating").value(5))
			.andExpect(jsonPath("$.content[0].content").value("목록 리뷰"));
	}

	@Test
	void 평점요약_조회_성공() throws Exception {
		UUID storeId = UUID.randomUUID();

		ResGetStoreRatingSummaryDtoV1 response = ResGetStoreRatingSummaryDtoV1.builder()
			.storeId(storeId)
			.reviewCount(2)
			.totalRatingSum(9)
			.averageRating(new BigDecimal("4.5"))
			.rating1Count(0)
			.rating2Count(0)
			.rating3Count(0)
			.rating4Count(1)
			.rating5Count(1)
			.build();

		when(reviewServiceV1.getStoreRatingSummary(storeId)).thenReturn(response);

		mockMvc.perform(get("/api/v1/stores/{storeId}/rating-summary", storeId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.storeId").value(storeId.toString()))
			.andExpect(jsonPath("$.reviewCount").value(2))
			.andExpect(jsonPath("$.totalRatingSum").value(9))
			.andExpect(jsonPath("$.averageRating").value(4.5))
			.andExpect(jsonPath("$.rating4Count").value(1))
			.andExpect(jsonPath("$.rating5Count").value(1));
	}

	@Test
	void 평점요약_수동갱신_성공() throws Exception {
		UUID managerId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/reviews/rating-summary/refresh")
				.with(loginUser(managerId, UserRole.MANAGER)))
			.andExpect(status().isOk())
			.andExpect(content().string("가게 평점 집계가 완료되었습니다."));

		verify(reviewRatingBatchService).refreshAllStoreRatings();
	}

	private RequestPostProcessor loginUser(UUID userId, UserRole role) {
		return request -> {
			AuthUser authUser = new AuthUser(userId, "test@test.com", role);

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(
					authUser,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
				);

			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);

			request.setUserPrincipal(authentication);

			return request;
		};
	}
}