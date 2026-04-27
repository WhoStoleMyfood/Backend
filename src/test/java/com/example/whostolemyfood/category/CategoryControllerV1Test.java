package com.example.whostolemyfood.category;


import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.category.presentation.controller.CategoryControllerV1;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryControllerV1.class)
@AutoConfigureMockMvc
@DisplayName("CategoryControllerV1 테스트")
class CategoryControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryServiceV1 categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private AuthUser testAuthUser;
    private Authentication testAuthentication;
    private UUID testCategoryId;
    private ReqCategoryDtoV1 categoryRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCategoryId = UUID.randomUUID();

        // ✅ AuthUser 생성 (MANAGER 역할)
        testAuthUser = new AuthUser(
                testUserId,
                "manager@example.com",
                UserRole.MANAGER
        );

        // ✅ Authentication 생성
        testAuthentication = new UsernamePasswordAuthenticationToken(
                testAuthUser,
                null,
                testAuthUser.getAuthorities()
        );


        categoryRequest = new ReqCategoryDtoV1("한식");
    }

    // ============ GET /api/v1/categories ============

    @Test
    @DisplayName("정상: 카테고리 목록 조회")
    void getCategories_Success() throws Exception {
        // given
        List<ResGetCategoryDtoV1> mockCategories = List.of(
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("한식")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("일식")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("중식")
                        .build()
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willReturn(mockCategories);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", equalTo("한식")))
                .andExpect(jsonPath("$[1].name", equalTo("일식")))
                .andExpect(jsonPath("$[2].name", equalTo("중식")));

        verify(categoryService, times(1)).getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        );
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - 빈 결과")
    void getCategories_Empty() throws Exception {
        // given
        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willReturn(List.of());

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ============ GET /api/v1/categories/{category_id} ============

    @Test
    @DisplayName("정상: 카테고리 상세 조회")
    void getCategory_Success() throws Exception {
        // given
        ResGetCategoryDtoV1 mockCategory = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식")
                .build();

        given(categoryService.getCategory(
                eq(testCategoryId),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willReturn(mockCategory);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories/{category_id}", testCategoryId)
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", equalTo(testCategoryId.toString())))
                .andExpect(jsonPath("$.name", equalTo("한식")));

        verify(categoryService, times(1)).getCategory(
                eq(testCategoryId),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        );
    }

    @Test
    @DisplayName("예외: 존재하지 않는 카테고리 조회")
    void getCategory_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(categoryService.getCategory(
                eq(nonExistentId),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willThrow(new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories/{category_id}", nonExistentId)
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions.andExpect(status().is4xxClientError());
    }

    // ============ POST /api/v1/categories ============

    @Test
    @DisplayName("정상: 카테고리 생성 - MANAGER 역할")
    void createCategory_Success() throws Exception {
        // given
        ResGetCategoryDtoV1 mockResponse = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식")
                .build();


        given(categoryService.createCategory(
                any(),  // ReqCategoryDtoV1
                any(),  // userId
                any()   // role
        )).willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                        post("/api/v1/categories")
                                .with(csrf())
                                .with(authentication(testAuthentication))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(categoryRequest))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("정상: 카테고리 생성 - MASTER 역할")
    void createCategory_MasterRole() throws Exception {
        // given
        AuthUser masterAuthUser = new AuthUser(
                UUID.randomUUID(),
                "master@example.com",
                UserRole.MASTER
        );

        Authentication masterAuthentication = new UsernamePasswordAuthenticationToken(
                masterAuthUser,
                null,
                masterAuthUser.getAuthorities()
        );

        ResGetCategoryDtoV1 mockResponse = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식")
                .build();

        given(categoryService.createCategory(
                any(ReqCategoryDtoV1.class),
                any(UUID.class),  // ← any() 사용 (UUID 검증 안함)
                any(String.class)  // ← any() 사용 (Role 문자열 검증 안함)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/categories")
                        .with(csrf())
                        .with(authentication(masterAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryRequest))
        );

        // then
        resultActions.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - CUSTOMER 역할 (권한 없음)")
    void createCategory_CustomerRole_Forbidden() throws Exception {
        // given
        AuthUser customerAuthUser = new AuthUser(
                UUID.randomUUID(),
                "customer@example.com",
                UserRole.CUSTOMER
        );

        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerAuthUser,
                null,
                customerAuthUser.getAuthorities()
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/categories")
                        .with(authentication(customerAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryRequest))
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 중복된 이름")
    void createCategory_Duplication() throws Exception {
        // given
        given(categoryService.createCategory(
                any(ReqCategoryDtoV1.class),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willThrow(new RuntimeException("CATEGORY_DUPLICATION"));

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/categories")
                        .with(authentication(testAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryRequest))
        );

        // then
        resultActions.andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("예외: 카테고리 생성 - 유효하지 않은 요청")
    void createCategory_InvalidRequest() throws Exception {
        // given
        ReqCategoryDtoV1 invalidRequest = new ReqCategoryDtoV1("");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/categories")
                        .with(csrf())
                        .with(authentication(testAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest))
        );

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    // ============ PUT /api/v1/categories/{category_id} ============

    @Test
    @DisplayName("정상: 카테고리 수정")
    void updateCategory_Success() throws Exception {
        // given
        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("한식 (수정됨)");

        ResGetCategoryDtoV1 mockResponse = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식 (수정됨)")
                .build();

        given(categoryService.updateCategory(
                eq(testCategoryId),
                any(ReqCategoryDtoV1.class),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/categories/{category_id}", testCategoryId)
                        .with(csrf())
                        .with(authentication(testAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId", equalTo(testCategoryId.toString())))
                .andExpect(jsonPath("$.name", equalTo("한식 (수정됨)")));

        verify(categoryService, times(1)).updateCategory(
                eq(testCategoryId),
                any(ReqCategoryDtoV1.class),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        );
    }

    @Test
    @DisplayName("예외: 카테고리 수정 - CUSTOMER 역할 (권한 없음)")
    void updateCategory_CustomerRole_Forbidden() throws Exception {
        // given
        AuthUser customerAuthUser = new AuthUser(
                UUID.randomUUID(),
                "customer@example.com",
                UserRole.CUSTOMER
        );

        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerAuthUser,
                null,
                customerAuthUser.getAuthorities()
        );

        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("한식 (수정됨)");

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/categories/{category_id}", testCategoryId)
                        .with(authentication(customerAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest))
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("예외: 카테고리 수정 - 존재하지 않는 카테고리")
    void updateCategory_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("한식 (수정됨)");

        given(categoryService.updateCategory(
                eq(nonExistentId),
                any(ReqCategoryDtoV1.class),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willThrow(new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/categories/{category_id}", nonExistentId)
                        .with(authentication(testAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest))
        );

        // then
        resultActions.andExpect(status().is4xxClientError());
    }

    // ============ DELETE /api/v1/categories/{category_id} ============

    @Test
    @DisplayName("정상: 카테고리 삭제")
    void deleteCategory_Success() throws Exception {
        // given
        willDoNothing().given(categoryService).deleteCategory(
                eq(testCategoryId),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/categories/{category_id}", testCategoryId)
                        .with(csrf())
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions.andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(
                eq(testCategoryId),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        );
    }

    @Test
    @DisplayName("예외: 카테고리 삭제 - CUSTOMER 역할 (권한 없음)")
    void deleteCategory_CustomerRole_Forbidden() throws Exception {
        // given
        AuthUser customerAuthUser = new AuthUser(
                UUID.randomUUID(),
                "customer@example.com",
                UserRole.CUSTOMER
        );

        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerAuthUser,
                null,
                customerAuthUser.getAuthorities()
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/categories/{category_id}", testCategoryId)
                        .with(authentication(customerAuthentication))
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("예외: 카테고리 삭제 - 존재하지 않는 카테고리")
    void deleteCategory_NotFound() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new IllegalArgumentException("존재하지 않는 카테고리입니다."))
                .when(categoryService)
                .deleteCategory(
                        eq(nonExistentId),
                        eq(testAuthUser.getUserId()),
                        eq(testAuthUser.role().name())
                );
        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/categories/{category_id}", nonExistentId)
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions.andExpect(status().is4xxClientError());
    }
}