package com.example.whostolemyfood.store.domain.entity;

import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreEntity extends BaseAuditEntity {

	@Id
	@Column(name = "store_id", nullable = false, updatable = false)
	private UUID storeId;

	@Column(name = "store_rating_id", nullable = false)
	private UUID storeRatingId;

	@Column(name = "name", nullable = false)
	private String name;
}