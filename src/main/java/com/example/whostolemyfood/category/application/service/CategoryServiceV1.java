package com.example.whostolemyfood.category.application.service;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceV1 {

    private final CategoryRepository categoryRepository;

    //카테고리 생성
    @Transactional
    public ResGetCategoryDtoV1 createCategory(ReqCategoryDtoV1 reqCategoryDto) {
        if (categoryRepository.existsByNameAndIsDeletedFalse(reqCategoryDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리입니다.");
        }
        CategoryEntity categoryEntity=CategoryEntity
                .builder()
                .categoryId(UUID.randomUUID())
                .name(reqCategoryDto.getName())
                .build();
        categoryEntity=categoryRepository.save(categoryEntity);
        return ResGetCategoryDtoV1.from(categoryEntity);
    }

    /**
     * 상세 카테고리 조회
     */
    public ResGetCategoryDtoV1 getCategory(UUID id) {
        return ResGetCategoryDtoV1.from(getCategoryById(id));
    }

    /**
     * 카테고리 목록 조회
     */
    public List<ResGetCategoryDtoV1> getCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> !category.getIsDeleted())
                .map(ResGetCategoryDtoV1::from) // Entity를 DTO로 변환
                .toList();
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public ResGetCategoryDtoV1 updateCategory(UUID id, ReqCategoryDtoV1 reqCategoryDto) {
        CategoryEntity categoryEntity = getCategoryById(id);

        categoryEntity.updateName(reqCategoryDto.getName());
        return ResGetCategoryDtoV1.from(categoryEntity);
    }

    /**
     * 카테고리 삭제 (soft delete)
     */
    @Transactional
    public void deleteCategory(UUID id) {
        CategoryEntity categoryEntity = getCategoryById(id);
        categoryEntity.softDelete();
    }


    /**
     * 카테고리 객체 조회
     */
    private CategoryEntity getCategoryById(UUID categoryId) {
        return categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }

}
