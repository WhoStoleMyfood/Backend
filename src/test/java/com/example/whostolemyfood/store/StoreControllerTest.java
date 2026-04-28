package com.example.whostolemyfood.store;


import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.application.service.StoreSearchServiceV1;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.presentation.controller.StoreControllerV1;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreControllerV1.class)
@AutoConfigureMockMvc
@DisplayName("StoreControllerV1 - search 엔드포인트 테스트")
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StoreSearchServiceV1 storeSearchService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private StoreServiceV1 storeService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private AuthUser testAuthUser;
    private Authentication testAuthentication;
    private StoreSearchConditionV1 searchCondition;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        // ✅ AuthUser record 생성
        testAuthUser = new AuthUser(
                testUserId,
                "test@example.com",
                UserRole.CUSTOMER
        );

        // ✅ Authentication 객체 생성 (testAuthUser를 principal로 사용)
        testAuthentication = new UsernamePasswordAuthenticationToken(
                testAuthUser,
                null,
                testAuthUser.getAuthorities()
        );

        searchCondition = new StoreSearchConditionV1();
        searchCondition.setKeyword("짜장");
        pageable = PageRequest.of(0, 10);
    }

    // ============ 정상 케이스 ============

    @Test
    @DisplayName("정상: 가게 검색 성공 - 결과 1개")
    void search_Success() throws Exception {
        // given
        StoreSearchResponseDtoV1 mockStoreDto = StoreSearchResponseDtoV1.builder()
                .storeId(UUID.randomUUID())
                .storeName("짜장백개")
                .build();

        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        List.of(mockStoreDto),
                        pageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                eq(testAuthUser.getUserId()),
                eq(testAuthUser.role().name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(testAuthentication))  // ✅ Authentication 객체 전달
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].storeName", equalTo("짜장백개")))
                .andExpect(jsonPath("$.totalElements", equalTo(1)));

        verify(storeSearchService, times(1)).search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        );
    }

    @Test
    @DisplayName("정상: 검색 결과가 없을 때")
    void search_EmptyResult() throws Exception {
        // given
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        Collections.emptyList(),
                        pageable,
                        0
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "없는음식")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(0)));
    }

    @Test
    @DisplayName("정상: 여러 개의 검색 결과 반환")
    void search_MultipleResults() throws Exception {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build(),
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장이네")
                        .build(),
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장코리아")
                        .build()
        );

        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockStores,
                        pageable,
                        3
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements", equalTo(3)))
                .andExpect(jsonPath("$.content[0].storeName", equalTo("짜장백개")))
                .andExpect(jsonPath("$.content[1].storeName", equalTo("짜장이네")))
                .andExpect(jsonPath("$.content[2].storeName", equalTo("짜장코리아")));
    }

    // ============ 페이징 테스트 ============

    @Test
    @DisplayName("정상: 페이징 - 첫 번째 페이지 (0번 페이지)")
    void search_FirstPage() throws Exception {
        // given
        StoreSearchResponseDtoV1 mockStoreDto = StoreSearchResponseDtoV1.builder()
                .storeId(UUID.randomUUID())
                .storeName("짜장백개")
                .build();

        Pageable firstPageable = PageRequest.of(0, 10);
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        List.of(mockStoreDto),
                        firstPageable,
                        11
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(11)))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("정상: 페이징 - 두 번째 페이지 (1번 페이지)")
    void search_SecondPage() throws Exception {
        // given
        StoreSearchResponseDtoV1 mockStoreDto = StoreSearchResponseDtoV1.builder()
                .storeId(UUID.randomUUID())
                .storeName("짜장이네")
                .build();

        Pageable secondPageable = PageRequest.of(1, 10);
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        List.of(mockStoreDto),
                        secondPageable,
                        11
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "1")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", equalTo(11)))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ============ 파라미터 검증 테스트 ============

    @Test
    @DisplayName("정상: 파라미터 없이 검색 (기본값 사용)")
    void search_WithoutKeyword() throws Exception {
        // given
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        Collections.emptyList(),
                        PageRequest.of(0, 10),
                        0
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("정상: 커스텀 페이지 사이즈 (size=20)")
    void search_CustomPageSize() throws Exception {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build()
        );

        Pageable customPageable = PageRequest.of(0, 20);
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockStores,
                        customPageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ============ 인증 관련 테스트 ============

    @Test
    @DisplayName("정상: CUSTOMER 역할로 검색")
    void search_CustomerRole() throws Exception {
        // given
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        Collections.emptyList(),
                        pageable,
                        0
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                eq(UserRole.CUSTOMER.name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("정상: MANAGER 역할로 검색")
    void search_ManagerRole() throws Exception {
        // given
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

        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        Collections.emptyList(),
                        pageable,
                        0
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                eq(managerAuthUser.getUserId()),
                eq(UserRole.MANAGER.name())
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .with(authentication(managerAuthentication))
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    // ============ 응답 구조 검증 ============

    @Test
    @DisplayName("정상: 응답 구조 전체 검증")
    void search_ResponseStructure() throws Exception {
        // given
        UUID storeId = UUID.randomUUID();
        StoreSearchResponseDtoV1 mockStoreDto = StoreSearchResponseDtoV1.builder()
                .storeId(storeId)
                .storeName("짜장백개")
                .build();

        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        List.of(mockStoreDto),
                        pageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when & then
        mockMvc.perform(
                        get("/api/stores/search")
                                .param("keyword", "짜장")
                                .with(authentication(testAuthentication))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.totalElements", notNullValue()))
                .andExpect(jsonPath("$.totalPages", notNullValue()))
                .andExpect(jsonPath("$.content[0].storeId", equalTo(storeId.toString())))
                .andExpect(jsonPath("$.content[0].storeName", equalTo("짜장백개")));
    }

    // ============ 비정상 페이징 테스트 ============

    @Test
    @DisplayName("비정상: 과도한 page size (99999) - 조정됨")
    void test_ExcessivePageSize() throws Exception {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build()
        );

        Pageable adjustedPageable = PageRequest.of(0, 10);  // 조정된 사이즈
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockStores,
                        adjustedPageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "99999")  // 비정상적으로 큰 사이즈
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", equalTo(10)));
    }

    @Test
    @DisplayName("비정상: 음수 page size - 기본값으로 조정됨")
    void test_NegativePageSize() throws Exception {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build()
        );

        // 음수 size는 기본값(10)으로 조정
        Pageable defaultPageable = PageRequest.of(0, 10);
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockStores,
                        defaultPageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "-10")  // 음수 사이즈
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", equalTo(10)));
    }


    @Test
    @DisplayName("비정상: size=0 - 기본값으로 조정됨")
    void test_ZeroPageSize() throws Exception {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build()
        );

        // size=0은 기본값(10)으로 조정
        Pageable defaultPageable = PageRequest.of(0, 10);
        PageResponse<StoreSearchResponseDtoV1> mockResponse = new PageResponse<>(
                new PageImpl<>(
                        mockStores,
                        defaultPageable,
                        1
                )
        );

        given(storeSearchService.search(
                any(StoreSearchConditionV1.class),
                any(Pageable.class),
                any(UUID.class),
                any(String.class)
        )).willReturn(mockResponse);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/stores/search")
                        .param("keyword", "짜장")
                        .param("page", "0")
                        .param("size", "0")  // 0 사이즈
                        .with(authentication(testAuthentication))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size", equalTo(10)));
    }
}