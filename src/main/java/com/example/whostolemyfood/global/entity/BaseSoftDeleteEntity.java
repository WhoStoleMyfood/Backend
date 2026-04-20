package com.example.whostolemyfood.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseTimeEntity {

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	public void softDelete() {
		this.isDeleted = true;
		this.deletedAt = LocalDateTime.now();
	}

	public void restore() {
		this.isDeleted = false;
		this.deletedAt = null;
	}
}