package com.example.whostolemyfood.category.application.service;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
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
    private final UserRepository userRepository;

    //카테고리 생성
    @Transactional
    public ResGetCategoryDtoV1 createCategory(ReqCategoryDtoV1 reqCategoryDto,UUID userId,String Role) {
        UserEntity loginUser = validateActiveUserAndRole(userId, Role);

        if (categoryRepository.existsByNameAndIsDeletedFalse(reqCategoryDto.getName())) {
            throw new CustomException(ErrorCode.CATEGORY_DUPLICATION);
        }
        CategoryEntity categoryEntity=CategoryEntity
                .builder()
                .name(reqCategoryDto.getName())
                .build();

        categoryEntity.markCreatedBy(loginUser.getId());
        categoryEntity=categoryRepository.save(categoryEntity);
        return ResGetCategoryDtoV1.from(categoryEntity);
    }

    /**
     * 상세 카테고리 조회
     */
    public ResGetCategoryDtoV1 getCategory(UUID id,UUID userId,String Role) {
        UserEntity loginUser = validateActiveUserAndRole(userId, Role);
        return ResGetCategoryDtoV1.from(getCategoryById(id));
    }

    /**
     * 카테고리 목록 조회
     */
    public List<ResGetCategoryDtoV1> getCategories(UUID userId,String Role) {
        UserEntity loginUser = validateActiveUserAndRole(userId, Role);

        return categoryRepository.findAll().stream()
                .filter(category -> !category.getIsDeleted())
                .map(ResGetCategoryDtoV1::from) // Entity를 DTO로 변환
                .toList();
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public ResGetCategoryDtoV1 updateCategory(UUID id, ReqCategoryDtoV1 reqCategoryDto,UUID userId,String Role) {
        UserEntity loginUser = validateActiveUserAndRole(userId, Role);

        CategoryEntity categoryEntity = getCategoryById(id);

        categoryEntity.updateName(reqCategoryDto.getName());
        categoryEntity.markUpdatedBy(loginUser.getId());
        return ResGetCategoryDtoV1.from(categoryEntity);
    }

    /**
     * 카테고리 삭제 (soft delete)
     */
    @Transactional
    public void deleteCategory(UUID id,UUID userId,String Role) {

        UserEntity loginUser = validateActiveUserAndRole(userId, Role);

        CategoryEntity categoryEntity = getCategoryById(id);

        categoryEntity.softDelete();
        categoryEntity.markUpdatedBy(loginUser.getId());
    }


    /**
     * 카테고리 객체 조회
     */
    private CategoryEntity getCategoryById(UUID categoryId) {
        return categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    }


    private UserEntity validateActiveUserAndRole(UUID loginUserId, String tokenRole) {
        UserEntity user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String Role = user.getUserRole().name();
        if (!Role.equals(tokenRole)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (user.getUserRole() != UserRole.MANAGER && user.getUserRole() != UserRole.MASTER){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return user;
    }

}
