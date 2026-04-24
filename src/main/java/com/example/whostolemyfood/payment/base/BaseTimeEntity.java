package com.example.whostolemyfood.payment.base;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseTimeEntity {

    @CreationTimestamp
    private LocalDateTime createdAt;

    private UUID createdBy;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private UUID updatedBy;


    public void setCreatedInfo(UUID userId) {
        this.createdBy = userId;
        this.updatedBy = userId;
    }

    public void setUpdatedInfo(UUID userId) {
        this.updatedBy = userId;
    }
}
