package com.example.whostolemyfood.category;

import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
import com.example.whostolemyfood.category.presentation.controller.CategoryControllerV1;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryControllerV1.class)
@DisplayName("Category Controller 단위 테스트 (Pageable 적용)")
class CategoryControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryServiceV1 categoryService;

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

        testAuthUser = new AuthUser(
                testUserId,
                "manager@example.com",
                UserRole.MANAGER
        );

        testAuthentication = new UsernamePasswordAuthenticationToken(
                testAuthUser,
                null,
                testAuthUser.getAuthorities()
        );

        categoryRequest = new ReqCategoryDtoV1("한식");
    }

    // ============ GET /api/v1/categories (페이징 적용) ============

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - 페이징 적용")
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

        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockCategories,
                        pageable,
                        3
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", equalTo("한식")))
                .andExpect(jsonPath("$.content[1].name", equalTo("일식")))
                .andExpect(jsonPath("$.content[2].name", equalTo("중식")))
                .andExpect(jsonPath("$.totalElements", equalTo(3)))
                .andExpect(jsonPath("$.totalPages", equalTo(1)));

        verify(categoryService, times(1)).getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - 빈 결과")
    void getCategories_Empty() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        Collections.emptyList(),
                        pageable,
                        0
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)))
                .andExpect(jsonPath("$.totalPages", equalTo(0)));
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - page 파라미터")
    void getCategories_WithPageParameter() throws Exception {
        // given
        List<ResGetCategoryDtoV1> mockCategories = List.of(
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("프랑스")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("스페인")
                        .build()
        );

        Pageable pageable = PageRequest.of(1, 10);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockCategories,
                        pageable,
                        22  // 총 22개 중 1번 페이지
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .param("page", "1")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", equalTo(22)))
                .andExpect(jsonPath("$.totalPages", equalTo(3)));
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - size 파라미터")
    void getCategories_WithSizeParameter() throws Exception {
        // given
        List<ResGetCategoryDtoV1> mockCategories = List.of(
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("한식")
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockCategories,
                        pageable,
                        1
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .param("size", "20")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - sort 파라미터")
    void getCategories_WithSortParameter() throws Exception {
        // given
        List<ResGetCategoryDtoV1> mockCategories = List.of(
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("중식")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("일식")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("한식")
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 10);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockCategories,
                        pageable,
                        3
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .param("sort", "name,desc")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].name", equalTo("중식")));
    }

    @Test
    @DisplayName("정상: 카테고리 목록 조회 - 페이징 복합 파라미터")
    void getCategories_WithComplexParameters() throws Exception {
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
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("태국식")
                        .build(),
                ResGetCategoryDtoV1.builder()
                        .categoryId(UUID.randomUUID())
                        .name("인도식")
                        .build()
        );

        Pageable pageable = PageRequest.of(0, 5);
        PageResponse<ResGetCategoryDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockCategories,
                        pageable,
                        15  // 총 15개 중 첫 5개
                )
        );

        given(categoryService.getCategories(
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name()),
                any(Pageable.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/categories")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", equalTo(15)))
                .andExpect(jsonPath("$.totalPages", equalTo(3)));
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

    // ============ POST /api/v1/categories ============

    @Test
    @DisplayName("정상: 카테고리 생성 성공")
    void createCategory_Success() throws Exception {

        AuthUser managerAuthUser = new AuthUser(
                UUID.randomUUID(),
                "manager@example.com",
                UserRole.MANAGER
        );

        Authentication managerAuthentication = new UsernamePasswordAuthenticationToken(
                managerAuthUser,
                null,
                managerAuthUser.getAuthorities()
        );
        // given
        ResGetCategoryDtoV1 mockResponse = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식")
                .build();

        given(categoryService.createCategory(
                any(),
                eq(managerAuthUser.userId()),
                eq(UserRole.MANAGER.name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/categories")
                        .with(csrf())
                        .with(authentication(managerAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(categoryRequest))
        );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.categoryId", equalTo(testCategoryId.toString())))
                .andExpect(jsonPath("$.name", equalTo("한식")));
    }

    // ============ PUT /api/v1/categories/{category_id} ============

    @Test
    @DisplayName("정상: 카테고리 수정")
    void updateCategory_Success() throws Exception {

        AuthUser managerAuthUser = new AuthUser(
                UUID.randomUUID(),
                "manager@example.com",
                UserRole.MANAGER
        );

        Authentication managerAuthentication = new UsernamePasswordAuthenticationToken(
                managerAuthUser,
                null,
                managerAuthUser.getAuthorities()
        );

        // given
        ReqCategoryDtoV1 updateRequest = new ReqCategoryDtoV1("한식 (수정됨)");

        ResGetCategoryDtoV1 mockResponse = ResGetCategoryDtoV1.builder()
                .categoryId(testCategoryId)
                .name("한식 (수정됨)")
                .build();

        given(categoryService.updateCategory(
                eq(testCategoryId),
                any(),
                eq(managerAuthUser.getUserId()),
                eq(UserRole.MANAGER.name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/categories/{category_id}", testCategoryId)
                        .with(csrf())
                        .with(authentication(managerAuthentication))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("한식 (수정됨)")));
    }

    // ============ DELETE /api/v1/categories/{category_id} ============

    @Test
    @DisplayName("정상: 카테고리 삭제")
    void deleteCategory_Success() throws Exception {
        // given
        doNothing().when(categoryService).deleteCategory(
                any(UUID.class),
                any(UUID.class),
                any(String.class)
        );

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/categories/{category_id}", testCategoryId)
                        .with(authentication(testAuthentication))  // ✅ @WithMockUser 대신 사용
                        .with(csrf())
        );

        // then
        resultActions.andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(
                any(UUID.class),
                any(UUID.class),
                any(String.class)
        );
    }
}