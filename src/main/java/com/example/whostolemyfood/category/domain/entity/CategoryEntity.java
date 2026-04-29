package com.example.whostolemyfood.category.domain.entity;


import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter

public class CategoryEntity extends BaseAuditEntity {

    // 카테고리 id
    @Id
    @Column(name="category_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID categoryId;

    // 카테고리 명
    @Column(name="name")
    private String name;

    @Builder
    public CategoryEntity(String name) {
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }

}
