package com.example.whostolemyfood.category;

import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Category Repository 단위 테스트")
class CategoryRepositoryV1Test {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private UUID categoryId1;
    private UUID categoryId2;
    private UUID categoryId3;

    @BeforeEach
    @Transactional
    void setUp() {
        // 테이블 초기화
        categoryRepository.deleteAll();
        em.flush();
        em.clear();

        // 테스트 데이터 생성
        CategoryEntity category1 = CategoryEntity.builder()
                .name("한식")
                .build();
        category1 = categoryRepository.save(category1);
        categoryId1 = category1.getCategoryId();

        CategoryEntity category2 = CategoryEntity.builder()
                .name("일식")
                .build();
        category2 = categoryRepository.save(category2);
        categoryId2 = category2.getCategoryId();

        CategoryEntity category3 = CategoryEntity.builder()
                .name("중식")
                .build();
        category3 = categoryRepository.save(category3);
        categoryId3 = category3.getCategoryId();

        em.flush();
        em.clear();
    }

    // ============ findByCategoryIdAndIsDeletedFalse ============

    @Test
    @Transactional
    @DisplayName("정상: 활성 카테고리 조회 성공")
    void findByCategoryIdAndIsDeletedFalse_Success() {
        // given
        // setUp에서 이미 생성됨

        // when
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("한식");
        assertThat(result.get().getIsDeleted()).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("정상: 존재하지 않는 카테고리 - 빈 Optional 반환")
    void findByCategoryIdAndIsDeletedFalse_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(nonExistentId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("정상: 삭제된 카테고리 조회 - 빈 Optional 반환")
    void findByCategoryIdAndIsDeletedFalse_DeletedCategory() {
        // given
        CategoryEntity category = categoryRepository.findById(categoryId1).orElseThrow();
        category.softDelete();
        categoryRepository.save(category);
        em.flush();
        em.clear();

        // when
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("정상: 여러 카테고리 중 특정 카테고리 조회")
    void findByCategoryIdAndIsDeletedFalse_SpecificCategory() {
        // given
        // setUp에서 3개 생성됨

        // when
        Optional<CategoryEntity> result2 = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId2);
        Optional<CategoryEntity> result3 = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId3);

        // then
        assertThat(result2).isPresent();
        assertThat(result2.get().getName()).isEqualTo("일식");

        assertThat(result3).isPresent();
        assertThat(result3.get().getName()).isEqualTo("중식");
    }

    // ============ existsByNameAndIsDeletedFalse ============

    @Test
    @Transactional
    @DisplayName("정상: 활성 카테고리 존재 확인 - true")
    void existsByNameAndIsDeletedFalse_Exists() {
        // given
        // setUp에서 이미 생성됨

        // when
        boolean result = categoryRepository.existsByNameAndIsDeletedFalse("한식");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("정상: 존재하지 않는 카테고리 - false")
    void existsByNameAndIsDeletedFalse_NotExists() {
        // given
        String nonExistentName = "없는음식";

        // when
        boolean result = categoryRepository.existsByNameAndIsDeletedFalse(nonExistentName);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("정상: 삭제된 카테고리는 존재하지 않는 것으로 판단")
    void existsByNameAndIsDeletedFalse_DeletedCategory() {
        // given
        CategoryEntity category = categoryRepository.findById(categoryId1).orElseThrow();
        category.softDelete();
        categoryRepository.save(category);
        em.flush();
        em.clear();

        // when
        boolean result = categoryRepository.existsByNameAndIsDeletedFalse("한식");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @Transactional
    @DisplayName("정상: 중복된 이름 체크 - 여러 활성 카테고리")
    void existsByNameAndIsDeletedFalse_MultipleActive() {
        // given
        // setUp에서 3개 생성됨

        // when
        boolean result1 = categoryRepository.existsByNameAndIsDeletedFalse("한식");
        boolean result2 = categoryRepository.existsByNameAndIsDeletedFalse("일식");
        boolean result3 = categoryRepository.existsByNameAndIsDeletedFalse("중식");

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(result3).isTrue();
    }

    // ============ softdelete + unique 통합 테스트 ============

    @Test
    @Transactional
    @DisplayName("정상: softdelete 후 같은 이름으로 재생성 가능")
    void softDeleteAndRecreateWithSameName() {
        // given
        String categoryName = "한식";
        UUID originalId = categoryId1;

        // 1. 원본 카테고리 조회
        CategoryEntity original = categoryRepository.findById(originalId).orElseThrow();
        assertThat(original.getName()).isEqualTo(categoryName);

        // 2. 원본 카테고리 softdelete
        original.softDelete();
        categoryRepository.save(original);
        em.flush();
        em.clear();

        // 3. softdelete 확인 - 삭제된 것으로 보임
        Optional<CategoryEntity> deletedResult = categoryRepository.findByCategoryIdAndIsDeletedFalse(originalId);
        assertThat(deletedResult).isEmpty();

        boolean existsAfterDelete = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);
        assertThat(existsAfterDelete).isFalse();

        // when
        // 4. 같은 이름으로 새 카테고리 생성
        CategoryEntity newCategory = CategoryEntity.builder()
                .name(categoryName)
                .build();
        newCategory = categoryRepository.save(newCategory);
        em.flush();
        em.clear();

        // then
        // 5. 새 카테고리 생성 확인
        assertThat(newCategory.getCategoryId()).isNotEqualTo(originalId);
        assertThat(newCategory.getName()).isEqualTo(categoryName);

        // 6. 새 카테고리 조회 가능
        Optional<CategoryEntity> recreatedResult = categoryRepository.findByCategoryIdAndIsDeletedFalse(newCategory.getCategoryId());
        assertThat(recreatedResult).isPresent();
        assertThat(recreatedResult.get().getName()).isEqualTo(categoryName);

        // 7. 활성 카테고리로 존재 확인
        boolean existsAfterRecreate = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);
        assertThat(existsAfterRecreate).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("정상: 하나는 활성, 하나는 삭제 상태 - 같은 이름 허용")
    void sameNameWithDifferentDeletedStatus() {
        // given
        String categoryName = "한식";

        // 1. 원본 카테고리 softdelete
        CategoryEntity original = categoryRepository.findById(categoryId1).orElseThrow();
        original.softDelete();
        categoryRepository.save(original);
        em.flush();

        // 2. 같은 이름으로 새 카테고리 생성 (아직 활성)
        CategoryEntity newCategory = CategoryEntity.builder()
                .name(categoryName)
                .build();
        newCategory = categoryRepository.save(newCategory);
        em.flush();
        em.clear();

        // when
        // 3. 활성 카테고리만 조회
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(newCategory.getCategoryId());
        boolean existsActive = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(categoryName);
        assertThat(existsActive).isTrue();

        // 4. 원본은 여전히 삭제 상태
        Optional<CategoryEntity> deletedResult = categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId1);
        assertThat(deletedResult).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("정상: 카테고리 삭제 후 동일 이름 재생성 여러 번")
    void multipleDeleteAndRecreate() {
        // given
        String categoryName = "한식";

        // 1. 첫 번째 생성 및 삭제
        CategoryEntity cat1 = categoryRepository.findById(categoryId1).orElseThrow();
        cat1.softDelete();
        categoryRepository.save(cat1);
        em.flush();

        // 2. 두 번째 생성
        CategoryEntity cat2 = CategoryEntity.builder()
                .name(categoryName)
                .build();
        cat2 = categoryRepository.save(cat2);
        UUID cat2Id = cat2.getCategoryId();
        em.flush();

        // 3. 두 번째 삭제
        cat2.softDelete();
        categoryRepository.save(cat2);
        em.flush();

        // when & then
        // 4. 세 번째 생성
        CategoryEntity cat3 = CategoryEntity.builder()
                .name(categoryName)
                .build();
        cat3 = categoryRepository.save(cat3);
        em.flush();
        em.clear();

        // 모두 다른 ID
        assertThat(cat3.getCategoryId()).isNotEqualTo(categoryId1);
        assertThat(cat3.getCategoryId()).isNotEqualTo(cat2Id);

        // 활성인 것은 cat3만
        boolean exists = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);
        assertThat(exists).isTrue();

        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(cat3.getCategoryId());
        assertThat(result).isPresent();
    }

    // ============ 엣지 케이스 ============

    @Test
    @Transactional
    @DisplayName("정상: 공백이 포함된 카테고리 이름")
    void categoryNameWithSpaces() {
        // given
        String categoryName = "한식 (밥 포함)";
        CategoryEntity category = CategoryEntity.builder()
                .name(categoryName)
                .build();
        category = categoryRepository.save(category);
        em.flush();
        em.clear();

        // when
        boolean exists = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(category.getCategoryId());

        // then
        assertThat(exists).isTrue();
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(categoryName);
    }

    @Test
    @Transactional
    @DisplayName("정상: 특수문자가 포함된 카테고리 이름")
    void categoryNameWithSpecialCharacters() {
        // given
        String categoryName = "한식/일식/중식";
        CategoryEntity category = CategoryEntity.builder()
                .name(categoryName)
                .build();
        category = categoryRepository.save(category);
        em.flush();
        em.clear();

        // when
        boolean exists = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("정상: 매우 긴 카테고리 이름")
    void categoryNameVeryLong() {
        // given
        String categoryName = "한식".repeat(50);  // 매우 긴 이름
        CategoryEntity category = CategoryEntity.builder()
                .name(categoryName)
                .build();
        category = categoryRepository.save(category);
        em.flush();
        em.clear();

        // when
        boolean exists = categoryRepository.existsByNameAndIsDeletedFalse(categoryName);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("정상: null 카테고리 ID로 조회")
    void findByCategoryIdAndIsDeletedFalse_NullId() {
        // given
        UUID nullId = null;

        // when & then
        // null ID로 조회하면 empty 반환
        Optional<CategoryEntity> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(nullId);
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("정상: 빈 문자열로 존재 여부 확인")
    void existsByNameAndIsDeletedFalse_EmptyString() {
        // given
        String emptyName = "";

        // when
        boolean exists = categoryRepository.existsByNameAndIsDeletedFalse(emptyName);

        // then
        assertThat(exists).isFalse();
    }
}
