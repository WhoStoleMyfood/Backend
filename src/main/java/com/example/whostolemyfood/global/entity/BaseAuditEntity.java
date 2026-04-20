package com.example.whostolemyfood.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseAuditEntity extends BaseSoftDeleteEntity {

	@Column(name = "created_by")
	private UUID createdBy;

	@Column(name = "updated_by")
	private UUID updatedBy;

	@Column(name = "deleted_by")
	private UUID deletedBy;

	public void markCreatedBy(UUID createdBy) {
		this.createdBy = createdBy;
	}

	public void markUpdatedBy(UUID updatedBy) {
		this.updatedBy = updatedBy;
	}

	public void softDelete(UUID deletedBy) {
		super.softDelete();
		this.deletedBy = deletedBy;
	}
}