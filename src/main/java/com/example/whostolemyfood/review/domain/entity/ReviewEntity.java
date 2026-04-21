package com.example.whostolemyfood.review.domain.entity;

import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
	name = "p_reviews",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_review_order_id", columnNames = "order_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewEntity extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "review_id", nullable = false, updatable = false)
	private UUID reviewId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private OrderEntity order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private StoreEntity store;

	@Column(name = "rating", nullable = false)
	private Integer rating;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	public void updateReview(Integer rating, String content, UUID updatedBy) {
		this.rating = rating;
		this.content = content;
		this.markUpdatedBy(updatedBy);
	}

	public void deleteReview(UUID deletedBy) {
		super.softDelete();
		this.markUpdatedBy(deletedBy);
	}

	public void restoreReview(Integer rating, String content, UUID updatedBy) {
		this.rating = rating;
		this.content = content;
		super.restore();
		this.markUpdatedBy(updatedBy);
	}
}