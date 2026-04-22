package com.example.whostolemyfood.category.domain.entity;


import com.example.whostolemyfood.global.entity.BaseAuditEntity;
import com.example.whostolemyfood.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_categories")
@NoArgsConstructor
@Getter
public class CategoryEntity extends BaseAuditEntity {

    // 카테고리 id
    @Id
    @Column(name="category_id")
    private UUID categoryId;

    // 카테고리 명
    @Column(name="name",unique = true)
    private String name;

    @Builder
    public CategoryEntity(UUID categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }



}
