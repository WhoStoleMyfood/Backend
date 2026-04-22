package com.example.whostolemyfood.category.domain.repository;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByCategoryIdAndIsDeletedFalse(UUID categoryId);

    boolean existsByNameAndIsDeletedFalse(@NotBlank(message = "카테고리명은 필수입니다.") String name);
}
