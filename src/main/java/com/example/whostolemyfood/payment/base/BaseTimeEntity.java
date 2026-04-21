package com.example.whostolemyfood.payment.base;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
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
public abstract class BaseTimeEntity {

    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreatedBy
    private UUID createdBy;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private UUID updatedBy;

}
