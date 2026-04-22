package com.example.whostolemyfood.category;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class) // Mockito 환경 설정
class CategoryServiceV1Test {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceV1 categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");
        CategoryEntity savedEntity = CategoryEntity.builder()
                .categoryId(UUID.randomUUID())
                .name("한식")
                .build();

        given(categoryRepository.save(any(CategoryEntity.class))).willReturn(savedEntity);

        // when
        ResGetCategoryDtoV1 result = categoryService.createCategory(request);

        // then
        assertThat(result.getName()).isEqualTo("한식");
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회 시 예외 발생")
    void getCategory_fail_notFound() {
        // given
        UUID invalidId = UUID.randomUUID();
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(invalidId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 카테고리입니다.");
    }

    @Test
    @DisplayName("카테고리 이름 수정 성공")
    void updateCategory_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("중식");

        CategoryEntity existingCategory = CategoryEntity.builder()
                .categoryId(categoryId)
                .name("한식")
                .build();

        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId))
                .willReturn(Optional.of(existingCategory));

        // when
        ResGetCategoryDtoV1 result = categoryService.updateCategory(categoryId,updateRequest);

        // then
        assertThat(result.getName()).isEqualTo("중식");
        // Dirty Checking으로 동작하므로 save() 호출 여부는 검증하지 않아도 됨
    }

    @Test
    @DisplayName("카테고리 삭제(Soft Delete) 호출 검증")
    void deleteCategory_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        CategoryEntity existingCategory = spy(CategoryEntity.builder()
                .categoryId(categoryId)
                .name("일식")
                .build());

        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId))
                .willReturn(Optional.of(existingCategory));

        // when
        categoryService.deleteCategory(categoryId);

        // then
        verify(existingCategory, times(1)).softDelete();
    }
}
