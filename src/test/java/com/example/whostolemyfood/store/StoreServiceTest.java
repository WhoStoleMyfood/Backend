package com.example.whostolemyfood.store;

import com.example.whostolemyfood.store.application.service.StoreSearchServiceV1;
import org.junit.jupiter.api.Test;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.store.domain.repository.StoreRepositoryCustom;
import com.example.whostolemyfood.store.presentation.dto.request.StoreSearchConditionV1;
import com.example.whostolemyfood.store.presentation.dto.response.StoreSearchResponseDtoV1;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreSearchServiceV1 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepositoryCustom storeRepositoryCustom;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StoreSearchServiceV1 storeSearchServiceV1;

    private UUID testUserId;
    private UserEntity testUser;
    private StoreSearchConditionV1 searchCondition;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = UserEntity.builder()
                .email("test@example.com")
                .name("테스트유저")
                .role(UserRole.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        searchCondition = new StoreSearchConditionV1();
        searchCondition.setKeyword("짜장");
        pageable = PageRequest.of(0, 10);
    }

    // ============ 정상 케이스 ============

    @Test
    @DisplayName("정상: 가게 검색 성공")
    void search_Success() {
        // given
        StoreSearchResponseDtoV1 mockStoreDto = StoreSearchResponseDtoV1.builder()
                .storeId(UUID.randomUUID())
                .storeName("짜장백개")
                .build();

        Page<StoreSearchResponseDtoV1> mockPage = new PageImpl<>(
                List.of(mockStoreDto),
                pageable,
                1
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));
        given(storeRepositoryCustom.searchStore(searchCondition, pageable))
                .willReturn(mockPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                UserRole.CUSTOMER.name()
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStoreName()).isEqualTo("짜장백개");
        assertThat(result.getTotalElements()).isEqualTo(1);

        // verify: 메서드 호출 확인
        verify(userRepository, times(1)).findById(testUserId);
        verify(storeRepositoryCustom, times(1)).searchStore(searchCondition, pageable);
    }

    @Test
    @DisplayName("정상: 검색 결과가 없을 때")
    void search_EmptyResult() {
        // given
        Page<StoreSearchResponseDtoV1> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));
        given(storeRepositoryCustom.searchStore(searchCondition, pageable))
                .willReturn(emptyPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                UserRole.CUSTOMER.name()
        );

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("정상: 여러 개의 검색 결과 반환")
    void search_MultipleResults() {
        // given
        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장백개")
                        .build(),
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장이네")
                        .build()
        );

        Page<StoreSearchResponseDtoV1> mockPage = new PageImpl<>(
                mockStores,
                pageable,
                2
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));
        given(storeRepositoryCustom.searchStore(searchCondition, pageable))
                .willReturn(mockPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                UserRole.CUSTOMER.name()
        );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ============ 예외 케이스 ============

    @Test
    @DisplayName("예외: 사용자를 찾을 수 없음")
    void search_UserNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(userRepository.findById(nonExistentUserId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeSearchServiceV1.search(
                searchCondition,
                pageable,
                nonExistentUserId,
                UserRole.CUSTOMER.name()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(storeRepositoryCustom, times(0)).searchStore(any(), any());
    }

    @Test
    @DisplayName("예외: 삭제된 사용자")
    void search_DeletedUser() {
        // given
        UUID userId = UUID.randomUUID();
        UserEntity deletedUser = UserEntity.builder()
                .email("deleted@example.com")
                .name("삭제된유저")
                .role(UserRole.CUSTOMER)
                .build();
        deletedUser.softDelete();
        ReflectionTestUtils.setField(deletedUser, "id", userId);

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(deletedUser));

        // when & then
        assertThatThrownBy(() -> storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                UserRole.CUSTOMER.name()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(storeRepositoryCustom, times(0)).searchStore(any(), any());
    }

    @Test
    @DisplayName("예외: 역할(Role)이 일치하지 않음")
    void search_RoleMismatch() {
        // given
        String differentRole = "MANAGER";  // 사용자는 USER인데 ADMIN으로 시도
        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                differentRole
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);

        verify(storeRepositoryCustom, times(0)).searchStore(any(), any());
    }

    @Test
    @DisplayName("예외: 잘못된 역할 형식")
    void search_InvalidRoleFormat() {
        // given
        String invalidRole = "INVALID_ROLE";
        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));

        // when & then
        assertThatThrownBy(() -> storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                invalidRole
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    // ============ 엣지 케이스 ============

    @Test
    @DisplayName("엣지: null 키워드로 검색")
    void search_NullKeyword() {
        // given
        StoreSearchConditionV1 nullKeywordCondition = new StoreSearchConditionV1();
        nullKeywordCondition.setKeyword(null);

        Page<StoreSearchResponseDtoV1> mockPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));
        given(storeRepositoryCustom.searchStore(nullKeywordCondition, pageable))
                .willReturn(mockPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                nullKeywordCondition,
                pageable,
                testUserId,
                UserRole.CUSTOMER.name()
        );

        // then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("엣지: 페이징 처리 (페이지 2)")
    void search_SecondPage() {
        // given
        Pageable secondPageable = PageRequest.of(1, 10);  // 두 번째 페이지

        List<StoreSearchResponseDtoV1> mockStores = List.of(
                StoreSearchResponseDtoV1.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("짜장코리아")
                        .build()
        );

        Page<StoreSearchResponseDtoV1> mockPage = new PageImpl<>(
                mockStores,
                secondPageable,
                11  // 총 11개 항목
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(testUser));
        given(storeRepositoryCustom.searchStore(searchCondition, secondPageable))
                .willReturn(mockPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                searchCondition,
                secondPageable,
                testUserId,
                UserRole.CUSTOMER.name()
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(11);
    }

    @Test
    @DisplayName("엣지: MANAGER 역할로 검색")
    void search_AdminRole() {
        // given
        UUID adminId = UUID.randomUUID();
        UserEntity adminUser = UserEntity.builder()
                .email("admin@example.com")
                .name("관리자")
                .role(UserRole.MANAGER)
                .build();

        ReflectionTestUtils.setField(adminUser, "id", adminId);

        Page<StoreSearchResponseDtoV1> mockPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
        );

        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(adminUser));
        given(storeRepositoryCustom.searchStore(searchCondition, pageable))
                .willReturn(mockPage);

        // when
        PageResponse<StoreSearchResponseDtoV1> result = storeSearchServiceV1.search(
                searchCondition,
                pageable,
                testUserId,
                UserRole.MANAGER.name()
        );

        // then
        assertThat(result).isNotNull();
        verify(storeRepositoryCustom, times(1)).searchStore(searchCondition, pageable);
    }
}
