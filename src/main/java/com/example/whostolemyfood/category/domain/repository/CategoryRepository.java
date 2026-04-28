package com.example.whostolemyfood.category.domain.repository;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param; // 추가

import java.util.Optional;
import java.util.UUID;

// 1. Long -> UUID로 변경 (Entity의 @Id 타입과 일치시켜야 함)
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Page<CategoryEntity> findByIsDeletedFalse(Pageable pageable);

    // 2. @Param 어노테이션 추가로 파라미터 명시
    Optional<CategoryEntity> findByCategoryIdAndIsDeletedFalse(@Param("categoryId") UUID categoryId);

    // 3. 존재 여부 확인 메서드에도 @Param 추가
    boolean existsByNameAndIsDeletedFalse(@Param("name") String name);
}