package com.example.whostolemyfood.store;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.area.domain.repository.AreaRepository;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.category.domain.repository.CategoryRepository;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.store.application.service.StoreServiceV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRatingSummaryRepository;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class StoreServiceV1Test {

    @InjectMocks
    private StoreServiceV1 storeServiceV1;

    @Mock private StoreRepository storeRepository;
    @Mock private StoreRatingSummaryRepository storeRatingSummaryRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AreaRepository areaRepository;



    // 테스트용 생성값
    private AuthUser createAuthUser (UUID userId, UserRole role) {
        return new AuthUser(userId, "owner@test.com", role);
    };

    private UserEntity createOwnerEntity (UUID userId) {
        UserEntity user = UserEntity.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private StoreEntity createStoreEntity (UUID storeId, String name, UserEntity owner) {
        StoreEntity store = StoreEntity.builder()
                .name(name)
                .address("기본주소")
                .category(CategoryEntity.builder().build())
                .area(AreaEntity.builder().isActive(true).build())
                .storeRatingSummary(StoreRatingSummaryEntity.builder().build())
                .phone("000-0000-0000")
                .content("기본 내용")
                .status(StoreStatus.OPEN)
                .minOrderPrice(20000)
                .openTime(LocalTime.of(10,0))
                .closeTime(LocalTime.of(23,0))
                .isHidden(false)
                .isDeleted(false)
                .build();

        ReflectionTestUtils.setField(store, "storeId", storeId);
        ReflectionTestUtils.setField(store, "user", owner);
        return store;
    }

    private ReqCreateStoreDtoV1 createStoreRequest(UUID categoryId, UUID areaId) {
        ReqCreateStoreDtoV1 request = new ReqCreateStoreDtoV1();
        ReflectionTestUtils.setField(request, "name", "스파르닭");
        ReflectionTestUtils.setField(request, "address", "내일시 배움구 캠프 5동");
        ReflectionTestUtils.setField(request,"categoryId", categoryId);
        ReflectionTestUtils.setField(request,"areaId", areaId);
        ReflectionTestUtils.setField(request, "phone", "000-0000-0000");
        ReflectionTestUtils.setField(request, "content", "This is spar닭");
        ReflectionTestUtils.setField(request, "minOrderPrice", 23000);
        ReflectionTestUtils.setField(request, "openTime", LocalTime.of(10, 0));
        ReflectionTestUtils.setField(request, "closeTime", LocalTime.of(23, 0));
        return request;
    }

    private ReqUpdateStoreDtoV1 updateStoreRequest(UUID categoryId, UUID areaId) {
        ReqUpdateStoreDtoV1 request = new ReqUpdateStoreDtoV1();
        ReflectionTestUtils.setField(request, "name", "수정이름");
        ReflectionTestUtils.setField(request, "address", "수정주소");
        ReflectionTestUtils.setField(request,"categoryId", categoryId);
        ReflectionTestUtils.setField(request,"areaId", areaId);
        ReflectionTestUtils.setField(request, "phone", "000-0000-0000");
        ReflectionTestUtils.setField(request,"content", "수정내용");
        ReflectionTestUtils.setField(request, "minOrderPrice", 20000);
        ReflectionTestUtils.setField(request, "openTime", LocalTime.of(10, 0));
        ReflectionTestUtils.setField(request, "closeTime", LocalTime.of(23, 0));
        return request;
    }

    @Test
    @DisplayName("[성공] 스토어 생성 시 모든 연관 관계가 검증되고 저정되어야한다")
    void createStoreSuccessTest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID areaId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(userId, UserRole.OWNER);
        ReqCreateStoreDtoV1 request = createStoreRequest(categoryId, areaId);

        UserEntity owner = createOwnerEntity(userId);
        CategoryEntity category = CategoryEntity.builder().build();
        AreaEntity area = AreaEntity.builder()
                .areaId(areaId)
                .isActive(true)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(owner));
        given(storeRepository.existsByNameAndIsDeletedFalse(any())).willReturn(false);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(areaRepository.findByAreaId(areaId)).willReturn(Optional.of(area));

        given(storeRepository.save(any(StoreEntity.class)))
                .willAnswer(invocation -> {
                    StoreEntity store = invocation.getArgument(0);
                    ReflectionTestUtils.setField(store,"storeId", storeId);
                    return store;
                });
        // when
        ResCreateStoreDtoV1 response = storeServiceV1.createStore(request, authUser);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("스파르닭");
        assertThat(response.getStoreId()).isEqualTo(storeId);
    }

    @Test
    @DisplayName("[실패] OWNER가 아닌 유저가 생성 시도 시 예외처리")
    void createStoreFailRoleTest() {
        // given
        AuthUser authUser = createAuthUser(UUID.randomUUID(), UserRole.CUSTOMER);
        ReqCreateStoreDtoV1 request = createStoreRequest(UUID.randomUUID(), UUID.randomUUID());
        // when & then
        CustomException expectedException = assertThrows(CustomException.class, () -> storeServiceV1.createStore(request, authUser));
        assertThat(expectedException.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("[실패] 활성화 안된 지역에 스토어 생성 시 예외 발생")
    void createStoreFailAreaNotActiveTest() {
        // given
        UUID userId = UUID.randomUUID();
        AuthUser authUser = createAuthUser(userId, UserRole.OWNER);
        ReqCreateStoreDtoV1 request = createStoreRequest(UUID.randomUUID(), UUID.randomUUID());

        AreaEntity area = AreaEntity.builder().isActive(false).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(createOwnerEntity(userId)));
        given(categoryRepository.findById(any())).willReturn(Optional.of(CategoryEntity.builder().build()));
        given(areaRepository.findByAreaId(any())).willReturn(Optional.of(area));
        // when & then
        CustomException expectedException = assertThrows(CustomException.class,
                () -> storeServiceV1.createStore(request, authUser));
        assertThat(expectedException.getErrorCode()).isEqualTo(ErrorCode.AREA_NOT_ACTIVE);
    }

    @Test
    @DisplayName("[실패] 이미 존재하는 이름으로 생성 시 예외처리")
    void createStoreFailExistNameTest() {
        // given
        UUID userId = UUID.randomUUID();
        AuthUser authUser = createAuthUser(UUID.randomUUID(), UserRole.OWNER);
        ReqCreateStoreDtoV1 request = createStoreRequest(UUID.randomUUID(), UUID.randomUUID());

        given(userRepository.findById(any())).willReturn(Optional.of(createOwnerEntity(userId)));
        given(storeRepository.existsByNameAndIsDeletedFalse(any())).willReturn(true);
        // when & then
        CustomException expectedException = assertThrows(CustomException.class, () -> storeServiceV1.createStore(request, authUser));
        assertThat(expectedException.getErrorCode()).isEqualTo(ErrorCode.STORE_DUPLICATION_NAME);
    }

    @Test
    @DisplayName("[실패] 숨겨지거나 삭제된 스토어는 조회 불가")
    void getStoreFailIsHiddenTest() {
        UUID storeId = UUID.randomUUID();

        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId))
                .willReturn(Optional.empty());

        CustomException expectedException = assertThrows(CustomException.class, () -> storeServiceV1.getStore(storeId));
        assertThat(expectedException.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_FOUND);
    }

    @Test
    @DisplayName("[성공] 스토어 아이디로 조회")
    void getStoreSuccessTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "sasdf", createOwnerEntity(UUID.randomUUID()));

        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId))
                .willReturn(Optional.of(store));

        ResGetStoreDtoV1 response = storeServiceV1.getStore(storeId);

        assertThat(response.getStoreId()).isEqualTo(storeId);
        assertThat(response.getName()).isEqualTo(store.getName());
    }

    @Test
    @DisplayName("[성공] 스토어 목록 조회 - 페이징 처리 완료")
    void getStoresSuccessTest() {
        Pageable pageable = PageRequest.of(0, 10);

        UserEntity owner = createOwnerEntity(UUID.randomUUID());

        List<StoreEntity> stores = List.of(
                createStoreEntity(UUID.randomUUID(), "store1", owner),
                createStoreEntity(UUID.randomUUID(), "store2", owner)
        );

        given(storeRepository.findAllByIsHiddenFalseAndIsDeletedFalse(pageable))
                .willReturn(new PageImpl<>(stores, pageable, stores.size()));

        Page<ResGetStoreListDtoV1> response = storeServiceV1.getStores(pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 스토어 숨김")
    void hiddenStoreSuccessTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "sasdf", createOwnerEntity(UUID.randomUUID()));
        AuthUser authUser = createAuthUser(store.getUser().getId(), UserRole.OWNER);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));

        storeServiceV1.hiddenStore(storeId, authUser);

        assertThat(store.getIsHidden()).isTrue();
    }

    // 메뉴아이디, 이름, 가격, 설명, 스토어
    @Test
    @DisplayName("[성공] 스토어 수정")
    void updateStoreSuccessTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "가게이름", createOwnerEntity(UUID.randomUUID()));
        AreaEntity area = AreaEntity.builder().isActive(true).build();
        CategoryEntity category = CategoryEntity.builder().build();
        AuthUser authUser = createAuthUser(store.getUser().getId(), UserRole.OWNER);
        ReqUpdateStoreDtoV1 request = updateStoreRequest(category.getCategoryId(), area.getAreaId());

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(storeRepository.existsByNameAndIsDeletedFalse(request.getName())).willReturn(false);
        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())).willReturn(Optional.of(CategoryEntity.builder().build()));

        storeServiceV1.updateStore(storeId,request,authUser);

        assertThat(store.getName()).isEqualTo("수정이름");
    }

    @Test
    @DisplayName("[실패] 수정할려는 이름이 이미 존재함")
    void updateStoreFailExistNameTest() {
        UUID storeId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
//        UserEntity owner = createOwnerEntity(UUID.randomUUID());
        StoreEntity store = createStoreEntity(storeId, "가게이름", createOwnerEntity(UUID.randomUUID()));
//        AreaEntity area = AreaEntity.builder().isActive(true).build();
//        CategoryEntity category = CategoryEntity.builder().build();
        AuthUser authUser = createAuthUser(store.getUser().getId(), UserRole.OWNER);
        ReqUpdateStoreDtoV1 request = updateStoreRequest(categoryId, UUID.randomUUID());

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(storeRepository.existsByNameAndIsDeletedFalse(request.getName())).willReturn(true);
//        given(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId())).willReturn(Optional.of(CategoryEntity.builder().build()));

//        storeServiceV1.updateStore(storeId,request,authUser);

        CustomException exception =  assertThrows(CustomException.class,
                () -> storeServiceV1.updateStore(storeId,request,authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_DUPLICATION_NAME);
    }

    @Test
    @DisplayName("[성공] 스토어 삭제")
    void deleteStoreSuccessTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "삭제될 스토어", createOwnerEntity(UUID.randomUUID()));
        AuthUser authUser = createAuthUser(store.getUser().getId(), UserRole.OWNER);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));

        storeServiceV1.deleteStore(storeId, authUser);
        assertThat(store.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("[실패] CUSTOMER는 스토어 관리에 접근할 수 없다")
    void validateStoreFailTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "권한없는 스토어", createOwnerEntity(UUID.randomUUID()));
        AuthUser authUser = createAuthUser(UUID.randomUUID(), UserRole.CUSTOMER);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));

        CustomException exception = assertThrows(CustomException.class, ()-> storeServiceV1.deleteStore(storeId, authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("[실패] 다른 OWNER는 내 스토어 관리에 접근할 수 없다")
    void validateStoreFailOwnerTest() {
        UUID storeId = UUID.randomUUID();
        StoreEntity store = createStoreEntity(storeId, "이건 내 스토어야", createOwnerEntity(UUID.randomUUID()));
        AuthUser authUser = createAuthUser(UUID.randomUUID(), UserRole.OWNER);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));

        CustomException exception = assertThrows(CustomException.class, () -> storeServiceV1.deleteStore(storeId, authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_NOT_OWNER);
    }
}
