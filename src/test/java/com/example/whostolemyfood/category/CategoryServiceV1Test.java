//package com.example.whostolemyfood.category;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.*;
//
//import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
//import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
//import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
//import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
//import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
//import com.example.whostolemyfood.user.application.security.AuthUser;
//import com.example.whostolemyfood.user.domain.entity.UserEntity;
//import com.example.whostolemyfood.user.domain.entity.UserRole;
//import com.example.whostolemyfood.user.domain.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryServiceV1Test {
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private CategoryServiceV1 categoryService;
//
//    private UUID userId;
//    private String userRoleStr = "MASTER";
//    private UserEntity testUser;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID();
//
//        // 엔티티의 @Builder 필드명(role, email, password, name)에 맞춰서 생성
//        testUser = UserEntity.builder()
//                .role(UserRole.MASTER)
//                .email("test@example.com")
//                .password("password")
//                .name("jieun")
//                .build();
//
//        // 엔티티 필드명이 'id'이므로 "id"로 세팅해야 합니다!
//        ReflectionTestUtils.setField(testUser, "id", userId);
//
//        // BaseAuditEntity나 상속받은 쪽에 isDeleted가 있다면 세팅
//        // (만약 UserEntity에 isDeleted가 없다면 이 줄은 제외하세요)
//        // ReflectionTestUtils.setField(testUser, "isDeleted", false);
//    }
//
//    @Test
//    @DisplayName("카테고리 생성 성공")
//    void createCategory_success() {
//        // given
//        ReqCategoryDtoV1 request = new ReqCategoryDtoV1("한식");
//        CategoryEntity savedEntity = CategoryEntity.builder()
//                .categoryId(UUID.randomUUID())
//                .name("한식")
//                .build();
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(categoryRepository.save(any(CategoryEntity.class))).willReturn(savedEntity);
//
//        // when
//        ResGetCategoryDtoV1 result = categoryService.createCategory(request, userId, userRoleStr);
//
//        // then
//        assertThat(result.getName()).isEqualTo("한식");
//        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
//    }
//
//    // ... getCategory_fail_notFound (이전과 동일)
//
//    @Test
//    @DisplayName("카테고리 이름 수정 성공")
//    void updateCategory_success() {
//        // given
//        UUID categoryId = UUID.randomUUID();
//        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("중식");
//
//        CategoryEntity existingCategory = CategoryEntity.builder()
//                .categoryId(categoryId)
//                .name("한식")
//                .build();
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId))
//                .willReturn(Optional.of(existingCategory));
//
//        // when
//        ResGetCategoryDtoV1 result = categoryService.updateCategory(categoryId, updateRequest, userRoleStr, userId);
//
//        // then
//        assertThat(result.getName()).isEqualTo("중식");
//    }
//
//    @Test
//    @DisplayName("카테고리 삭제(Soft Delete) 호출 검증")
//    void deleteCategory_success() {
//        // given
//        UUID categoryId = UUID.randomUUID();
//        CategoryEntity existingCategory = spy(CategoryEntity.builder()
//                .categoryId(categoryId)
//                .name("일식")
//                .build());
//
//        ReflectionTestUtils.setField(existingCategory, "isDeleted", false);
//
//        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
//        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(categoryId))
//                .willReturn(Optional.of(existingCategory));
//
//        // when
//        categoryService.deleteCategory(categoryId, userRoleStr, userId);
//
//        // then
//        assertThat(existingCategory.getIsDeleted()).isTrue();
//    }
//}}