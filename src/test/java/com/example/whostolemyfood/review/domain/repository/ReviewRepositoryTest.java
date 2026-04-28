package com.example.whostolemyfood.review.domain.repository;

import com.example.whostolemyfood.review.domain.entity.ReviewEntity;
import com.example.whostolemyfood.review.presentation.dto.request.ReqGetReviewsDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.repository.StoreRatingSummaryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewRepositoryTest {

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private StoreRatingSummaryRepository storeRatingSummaryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void 리뷰ID로_삭제되지않은_리뷰를_조회한다() {
		TestData data = insertTestData(false, 5);

		Optional<ReviewEntity> result =
			reviewRepository.findByReviewIdAndIsDeletedFalse(data.reviewId());

		assertThat(result).isPresent();
		assertThat(result.get().getRating()).isEqualTo(5);
		assertThat(result.get().getContent()).isEqualTo("리뷰 테스트");
	}

	@Test
	void 삭제된_리뷰는_활성리뷰_조회에서_제외된다() {
		TestData data = insertTestData(true, 4);

		Optional<ReviewEntity> result =
			reviewRepository.findByReviewIdAndIsDeletedFalse(data.reviewId());

		assertThat(result).isEmpty();
	}

	@Test
	void 주문ID로_활성리뷰_존재여부를_확인한다() {
		TestData data = insertTestData(false, 5);

		boolean exists =
			reviewRepository.existsByOrder_OrderIdAndIsDeletedFalse(data.orderId());

		assertThat(exists).isTrue();
	}

	@Test
	void 주문ID로_삭제된_리뷰를_조회한다() {
		TestData data = insertTestData(true, 3);

		Optional<ReviewEntity> result =
			reviewRepository.findByOrder_OrderIdAndIsDeletedTrue(data.orderId());

		assertThat(result).isPresent();
		assertThat(result.get().getRating()).isEqualTo(3);
	}

	@Test
	void 리뷰_검색_storeId_rating_조건으로_조회한다() {
		TestData data = insertTestData(false, 5);

		ReqGetReviewsDtoV1 condition = new ReqGetReviewsDtoV1();
		ReflectionTestUtils.setField(condition, "storeId", data.storeId());
		ReflectionTestUtils.setField(condition, "rating", 5);
		ReflectionTestUtils.setField(condition, "page", 0);
		ReflectionTestUtils.setField(condition, "size", 10);
		ReflectionTestUtils.setField(condition, "sort", "createdAt,DESC");

		Page<ReviewEntity> result = reviewRepository.search(condition, condition.toPageable());

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getStore().getStoreId()).isEqualTo(data.storeId());
		assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
	}

	@Test
	void 평점요약을_ID와_isDeletedFalse로_조회한다() {
		UUID summaryId = UUID.randomUUID();

		jdbcTemplate.update("""
            INSERT INTO p_store_rating_summarys (
                store_rating_id,
                review_count,
                total_rating_sum,
                average_rating,
                rating_1_count,
                rating_2_count,
                rating_3_count,
                rating_4_count,
                rating_5_count,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, 0, 0, 0.0, 0, 0, 0, 0, 0, false, now(), now())
            """, summaryId);

		Optional<StoreRatingSummaryEntity> result =
			storeRatingSummaryRepository.findByIdAndIsDeletedFalse(summaryId);

		assertThat(result).isPresent();
		assertThat(result.get().getReviewCount()).isEqualTo(0);
	}

	private TestData insertTestData(boolean reviewDeleted, int rating) {
		UUID customerId = UUID.randomUUID();
		UUID ownerId = UUID.randomUUID();
		UUID addressId = UUID.randomUUID();
		UUID areaId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		UUID storeRatingId = UUID.randomUUID();
		UUID storeId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID reviewId = UUID.randomUUID();

		jdbcTemplate.update("""
            INSERT INTO p_users (
                user_id,
                role,
                user_email,
                user_password,
                user_name,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, 'CUSTOMER', ?, 'password', '고객테스트', false, now(), now())
            """, customerId, "customer-" + customerId + "@test.com");

		jdbcTemplate.update("""
            INSERT INTO p_users (
                user_id,
                role,
                user_email,
                user_password,
                user_name,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, 'OWNER', ?, 'password', '오너테스트', false, now(), now())
            """, ownerId, "owner-" + ownerId + "@test.com");

		jdbcTemplate.update("""
            INSERT INTO p_areas (
                area_id,
                uk_name,
                city,
                district,
                is_active,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, ?, '서울특별시', '종로구', true, false, now(), now())
            """, areaId, "광화문-" + areaId);

		jdbcTemplate.update("""
            INSERT INTO p_categories (
                category_id,
                name,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, ?, false, now(), now())
            """, categoryId, "한식-" + categoryId);

		jdbcTemplate.update("""
            INSERT INTO p_store_rating_summarys (
                store_rating_id,
                review_count,
                total_rating_sum,
                average_rating,
                rating_1_count,
                rating_2_count,
                rating_3_count,
                rating_4_count,
                rating_5_count,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, 0, 0, 0.0, 0, 0, 0, 0, 0, false, now(), now())
            """, storeRatingId);

		jdbcTemplate.update("""
            INSERT INTO p_stores (
                store_id,
                user_id,
                category_id,
                area_id,
                store_rating_id,
                name,
                address,
                phone,
                content,
                min_order_price,
                status,
                open_time,
                close_time,
                is_hidden,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, '서울 종로구', '010-1111-2222', '테스트 가게',
                    10000, 'OPEN', '09:00', '21:00', false, false, now(), now())
            """, storeId, ownerId, categoryId, areaId, storeRatingId, "테스트가게-" + storeId);

		jdbcTemplate.update("""
            INSERT INTO p_address (
                address_id,
                user_id,
                alias,
                address,
                detail,
                zip_code,
                is_default,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, ?, '집', '서울 종로구', '101호', '03172', true, false, now(), now())
            """, addressId, customerId);

		jdbcTemplate.update("""
            INSERT INTO p_orders (
                order_id,
                user_id,
                address_id,
                store_id,
                request,
                total_price,
                status,
                delivery_fee,
                is_deleted,
                is_hidden,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, '리뷰 테스트용 주문', 15000, 'COMPLETED',
                    0, false, false, now(), now())
            """, orderId, customerId, addressId, storeId);

		jdbcTemplate.update("""
            INSERT INTO p_reviews (
                review_id,
                order_id,
                user_id,
                store_id,
                rating,
                content,
                is_deleted,
                created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, '리뷰 테스트', ?, now(), now())
            """, reviewId, orderId, customerId, storeId, rating, reviewDeleted);

		return new TestData(reviewId, orderId, storeId);
	}

	private record TestData(
		UUID reviewId,
		UUID orderId,
		UUID storeId
	) {
	}
}