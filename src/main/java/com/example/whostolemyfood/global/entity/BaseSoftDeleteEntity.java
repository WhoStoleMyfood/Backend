package com.example.whostolemyfood.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseAuditEntity {
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    private UUID deletedBy;

    public void delete(UUID deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}