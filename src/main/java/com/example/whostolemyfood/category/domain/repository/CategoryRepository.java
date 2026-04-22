package com.example.whostolemyfood.category.domain.repository;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByCategoryIdAndIsDeletedFalse(UUID categoryId);
}
