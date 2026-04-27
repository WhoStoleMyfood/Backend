package com.example.whostolemyfood.category;

import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceV1 테스트")
class CategoryServiceV1Test {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryServiceV1 categoryService;

    private UUID testUserId;
    private UUID testCategoryId;
    private UserEntity testUser;
    private UserEntity managerUser;
    private UserEntity masterUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCategoryId = UUID.randomUUID();

        // 일반 사용자 (권한 없음)
        testUser = UserEntity.builder()
                .email("user@example.com")
                .name("일반사용자")
                .role(UserRole.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        // 매니저
        UUID managerId = UUID.randomUUID();
        managerUser = UserEntity.builder()
                .email("manager@example.com")
                .name("매니저")
                .role(UserRole.MANAGER)
                .build();
        ReflectionTestUtils.setField(managerUser, "id", managerId);

        // 마스터
        UUID masterId = UUID.randomUUID();
        masterUser = UserEntity.builder()
                .email("master@example.com")
                .name("마스터")
                .role(UserRole.MASTER)
                .build();
        ReflectionTestUtils.setField(masterUser, "id", masterId);
    }

    // ============ createCategory ============

    @Test
    @DisplayName("정상: 카테고리 생성 성공 - MANAGER")
    void createCategory_Success_Manager() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.existsByNameAndIsDeletedFalse("한식"))
                .willReturn(false);
        given(categoryRepository.save(any(CategoryEntity.class)))
                .willAnswer(invocation -> {
                    CategoryEntity entity = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entity, "categoryId", testCategoryId);
                    return entity;
                });

        // when
        ResGetCategoryDtoV1 result = categoryService.createCategory(
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("한식");
        verify(categoryRepository, times(1)).existsByNameAndIsDeletedFalse("한식");
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("정상: 카테고리 생성 성공 - MASTER")
    void createCategory_Success_Master() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("일식");

        given(userRepository.findById(masterUser.getId()))
                .willReturn(Optional.of(masterUser));
        given(categoryRepository.existsByNameAndIsDeletedFalse("일식"))
                .willReturn(false);
        given(categoryRepository.save(any(CategoryEntity.class)))
                .willAnswer(invocation -> {
                    CategoryEntity entity = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entity, "categoryId", testCategoryId);
                    return entity;
                });

        // when
        ResGetCategoryDtoV1 result = categoryService.createCategory(
                request,
                masterUser.getId(),
                UserRole.MASTER.name()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("일식");
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 중복된 이름")
    void createCategory_Duplication() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.existsByNameAndIsDeletedFalse("한식"))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(CustomException.class);

        verify(categoryRepository, times(0)).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 사용자를 찾을 수 없음")
    void createCategory_UserNotFound() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        UUID nonExistentId = UUID.randomUUID();
        given(userRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                nonExistentId,
                UserRole.MANAGER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("예외: 활성 카테고리가 이미 존재하면 중복 에러")
    void createCategory_ActiveCategoryExists_Duplication() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        // 활성 카테고리가 이미 존재
        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.existsByNameAndIsDeletedFalse("한식"))
                .willReturn(true);  // 이미 활성 카테고리 존재

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_DUPLICATION);

        // 저장되면 안됨
        verify(categoryRepository, times(0)).save(any(CategoryEntity.class));
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 삭제된 사용자")
    void createCategory_DeletedUser() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        UserEntity deletedUser = UserEntity.builder()
                .email("deleted@example.com")
                .name("삭제된사용자")
                .role(UserRole.MANAGER)
                .build();
        deletedUser.softDelete();
        ReflectionTestUtils.setField(deletedUser, "id", managerUser.getId());

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(deletedUser));

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 역할 불일치")
    void createCategory_RoleMismatch() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));

        // when & then (토큰의 역할이 다름)
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                managerUser.getId(),
                UserRole.CUSTOMER.name()  // 불일치
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 권한 없음 (CUSTOMER)")
    void createCategory_NoPermission() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");

        given(userRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(
                request,
                testUser.getId(),
                UserRole.CUSTOMER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    // ============ getCategory ============

    @Test
    @DisplayName("정상: 카테고리 상세 조회")
    void getCategory_Success() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("한식")
                .build();
        ReflectionTestUtils.setField(category, "categoryId", testCategoryId);

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(testCategoryId))
                .willReturn(Optional.of(category));

        // when
        ResGetCategoryDtoV1 result = categoryService.getCategory(
                testCategoryId,
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("한식");
        verify(categoryRepository, times(1)).findByCategoryIdAndIsDeletedFalse(testCategoryId);
    }

    @Test
    @DisplayName("예외: 카테고리 상세 조회 - 존재하지 않음")
    void getCategory_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(nonExistentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(
                nonExistentId,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    // ============ getCategories ============

    @Test
    @DisplayName("정상: 카테고리 목록 조회")
    void getCategories_Success() {
        // given
        CategoryEntity category1 = CategoryEntity.builder().name("한식").build();
        CategoryEntity category2 = CategoryEntity.builder().name("일식").build();
        CategoryEntity category3 = CategoryEntity.builder().name("중식").build();

        ReflectionTestUtils.setField(category1, "categoryId", UUID.randomUUID());
        ReflectionTestUtils.setField(category2, "categoryId", UUID.randomUUID());
        ReflectionTestUtils.setField(category3, "categoryId", UUID.randomUUID());

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findAll())
                .willReturn(List.of(category1, category2, category3));

        // when
        List<ResGetCategoryDtoV1> result = categoryService.getCategories(
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("한식");
        assertThat(result.get(1).getName()).isEqualTo("일식");
        assertThat(result.get(2).getName()).isEqualTo("중식");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - 빈 결과")
    void getCategories_Empty() {
        // given
        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findAll())
                .willReturn(List.of());

        // when
        List<ResGetCategoryDtoV1> result = categoryService.getCategories(
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).isEmpty();
    }

    // ============ updateCategory ============

    @Test
    @DisplayName("정상: 카테고리 수정 성공")
    void updateCategory_Success() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식 (수정됨)");

        CategoryEntity category = CategoryEntity.builder()
                .name("한식")
                .build();
        ReflectionTestUtils.setField(category, "categoryId", testCategoryId);

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(testCategoryId))
                .willReturn(Optional.of(category));

        // when
        ResGetCategoryDtoV1 result = categoryService.updateCategory(
                testCategoryId,
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("한식 (수정됨)");
        verify(categoryRepository, times(1)).findByCategoryIdAndIsDeletedFalse(testCategoryId);
    }

    @Test
    @DisplayName("예외: 카테고리 수정 - 존재하지 않음")
    void updateCategory_NotFound() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식 (수정됨)");

        UUID nonExistentId = UUID.randomUUID();
        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(nonExistentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(
                nonExistentId,
                request,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예외: 카테고리 수정 - 권한 없음")
    void updateCategory_NoPermission() {
        // given
        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식 (수정됨)");

        given(userRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(
                testCategoryId,
                request,
                testUser.getId(),
                UserRole.CUSTOMER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    // ============ deleteCategory ============

    @Test
    @DisplayName("정상: 카테고리 삭제 성공")
    void deleteCategory_Success() {
        // given
        CategoryEntity category = CategoryEntity.builder()
                .name("한식")
                .build();
        ReflectionTestUtils.setField(category, "categoryId", testCategoryId);

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(testCategoryId))
                .willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(
                testCategoryId,
                managerUser.getId(),
                UserRole.MANAGER.name()
        );

        // then
        verify(categoryRepository, times(1)).findByCategoryIdAndIsDeletedFalse(testCategoryId);
        assertThat(category.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("예외: 카테고리 삭제 - 존재하지 않음")
    void deleteCategory_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        given(userRepository.findById(managerUser.getId()))
                .willReturn(Optional.of(managerUser));
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(nonExistentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(
                nonExistentId,
                managerUser.getId(),
                UserRole.MANAGER.name()
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예외: 카테고리 삭제 - 권한 없음")
    void deleteCategory_NoPermission() {
        // given
        given(userRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(
                testCategoryId,
                testUser.getId(),
                UserRole.CUSTOMER.name()
        )).isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }
}
