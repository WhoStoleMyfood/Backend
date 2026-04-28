package com.example.whostolemyfood.menu;

import com.example.whostolemyfood.ai.application.service.AiLogServiceV1;
import com.example.whostolemyfood.ai.presentation.dto.response.ResGetAiLogDtoV1;
import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import com.example.whostolemyfood.category.domain.entity.CategoryEntity;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqUpdateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResGetMenuDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreRatingSummaryEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @InjectMocks
    private MenuServiceV1 menuServiceV1;

    @Mock private MenuRepository menuRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private AiLogServiceV1 aiLogServiceV1;

    // 테스트용 생성값
    private AuthUser createAuthUser(UUID userId, UserRole role) {
        return new AuthUser(userId, "owner@test.com", role);
    };

    private UserEntity createOwnerEntity(UUID userId) {
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

    private MenuEntity createMenuEntity(UUID menuId, String name, Integer price, String description, StoreEntity store) {
        MenuEntity menu = MenuEntity.builder()
                .store(store)
                .name(name)
                .price(price)
                .description(description)
                .aiLogId(null)
                .isHidden(false)
                .isDeleted(false)
                .build();
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        return menu;
    }

    private ReqCreateMenuDtoV1 createReqCreateMenuDtoV1() {
        ReqCreateMenuDtoV1 request = new ReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request, "name", "황금 올리브 치킨");
        ReflectionTestUtils.setField(request,"price", 23000);
        ReflectionTestUtils.setField(request, "description", "올리브유로 튀긴 후라이드 치킨");
        ReflectionTestUtils.setField(request, "aiDescription", false);
        ReflectionTestUtils.setField(request, "aiPrompt", null);
        return request;
    }

    private ReqUpdateMenuDtoV1 createReqUpdateMenuDtoV1() {
        ReqUpdateMenuDtoV1 request = new ReqUpdateMenuDtoV1();
        ReflectionTestUtils.setField(request, "name", "updateName");
        ReflectionTestUtils.setField(request, "price", 25000);
        ReflectionTestUtils.setField(request, "description", "updateDescription");
        return  request;
    }

    @Test
    @DisplayName("[성공] 오너의 설명으로 메뉴 생성")
    void addMenuSuccessTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
        AuthUser authUser = createAuthUser(ownerId, UserRole.OWNER);
        StoreEntity store = createStoreEntity(storeId,"테스트 가게", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();

        MenuEntity menu = createMenuEntity(menuId,request.getName(),request.getPrice(),request.getDescription(),store);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(false);
        given(menuRepository.save(any(MenuEntity.class))).willReturn(menu);

        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId,request,authUser);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(menu.getName());
        assertThat(response.getPrice()).isEqualTo(request.getPrice());
        assertThat(response.getDescription()).isEqualTo(menu.getDescription());
    }

    @Test
    @DisplayName("[성공] Ai를 통해 설명을 작성 받는다")
    void addMenuSuccessAiDescriptionTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID aiLogId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
        AuthUser authUser = createAuthUser(ownerId, UserRole.OWNER);
        StoreEntity store = createStoreEntity(storeId, "테스트 가게", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request,"description", null);
        ReflectionTestUtils.setField(request, "aiDescription", true);
        ReflectionTestUtils.setField(request, "aiPrompt", "올리브유로 튀킨 치킨에 대해 설명을 작성해줘");

        String aiDescription = "올리브유로 바삭하게 튀긴 고소한 후라이드 치킨입니다";

        MenuEntity menu = createMenuEntity(
                menuId,
                request.getName(),
                request.getPrice(),
                aiDescription,
                store);

        ReflectionTestUtils.setField(menu, "aiLogId", aiLogId);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName()))
                .willReturn(false);
        given(aiLogServiceV1.generateMenuDescription(ownerId, request.getAiPrompt()))
                .willReturn(new ResGetAiLogDtoV1(aiDescription,aiLogId));
        given(menuRepository.save(any(MenuEntity.class))).willReturn(menu);

        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId,request,authUser);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getPrice()).isEqualTo(request.getPrice());
        assertThat(response.getDescription()).isEqualTo(aiDescription);
    }

    @Test
    @DisplayName("[실패] 같은 이름의 메뉴가 스토어에 존재한다면 추가할 수 없다")
    void addMenuFailTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId, UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"테스트 가게", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request, "name" ,"중복된 이름");

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> menuServiceV1.addMenu(storeId,request,authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_DUPLICATION_NAME);
    }
    @Test
    @DisplayName("[성공] 메뉴 아이디로 조회")
    void getMenuSuccessTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
//        AuthUser authUser = createAuthUser(ownerId, UserRole.OWNER);
        StoreEntity store = createStoreEntity(storeId,"테스트 가게", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();

        MenuEntity menu = createMenuEntity(
                menuId,
                request.getName(),
                request.getPrice(),
                request.getDescription(),store);

        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId))
                .willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(menuId, storeId))
        .willReturn(Optional.of(menu));

        ResGetMenuDtoV1 menuResponse = menuServiceV1.getMenu(storeId,menuId);

        assertThat(menuResponse).isNotNull();
        assertThat(menuResponse.getName()).isEqualTo(menu.getName());
        assertThat(menuResponse.getPrice()).isEqualTo(menu.getPrice());
    }

    @Test
    @DisplayName("[실패] 숨겨진 메뉴는 조회할 수 없다")
    void isHiddenMenuGetFailTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId, "테스트 가게", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();

        MenuEntity menu = createMenuEntity(
                menuId,
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                store);

        ReflectionTestUtils.setField(menu, "isHidden", true);

        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> menuServiceV1.getMenu(storeId,menuId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_NOT_FOUND);
    }

    @Test
    @DisplayName("[성공] 스토어 목록 조회 - 페이징")
    void getMenusSuccessTest() {
        Pageable pageable = PageRequest.of(0, 10);

        UserEntity owner = createOwnerEntity(UUID.randomUUID());
        StoreEntity store = createStoreEntity(UUID.randomUUID(),"test store", owner);

        List<MenuEntity> menus = List.of(
                createMenuEntity(UUID.randomUUID(), "menu1", 10000,"menu1's des", store),
                createMenuEntity(UUID.randomUUID(), "menu2", 10000,"menu2's des", store),
                createMenuEntity(UUID.randomUUID(), "menu3", 10000,"menu3's des", store));

        given(menuRepository.findAllByIsHiddenFalseAndIsDeletedFalse(pageable))
                .willReturn(new PageImpl<>(menus, pageable, menus.size()));

        Page<ResGetMenuDtoV1> response = menuServiceV1.getMenus(pageable);

        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("[성공] 스토어 수정")
    void updateMenuSuccessTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);
        MenuEntity menu = createMenuEntity(menuId,"menu1", 10000,"menu1's des", store);

        ReqUpdateMenuDtoV1 request = createReqUpdateMenuDtoV1();

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.of(menu));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(false);

        menuServiceV1.updateMenu(storeId,menuId,request,authUser);

        assertThat(menu.getName()).isEqualTo("updateName");
    }

    @Test
    @DisplayName("[실패] 이미 존재하는 이름으로 메뉴이름을 수정할 수 없다")
    void updateMenuFailExistNameTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);
        MenuEntity menu = createMenuEntity(menuId,"menu1", 10000,"menu1's des", store);

        ReqUpdateMenuDtoV1 request = createReqUpdateMenuDtoV1();

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.of(menu));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> menuServiceV1.updateMenu(storeId,menuId,request,authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_DUPLICATION_NAME);
    }

    @Test
    @DisplayName("[성공] 메뉴를 숨길 수 있다")
    void hiddenMenuSuccessTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);
        MenuEntity menu = createMenuEntity(menuId,"menu1", 10000,"menu1's des", store);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.of(menu));

        menuServiceV1.hiddenMenu(storeId,menuId,authUser);
        assertThat(menu.getIsHidden()).isTrue();
    }

    @Test
    @DisplayName("[성공] 메뉴를 삭제할 수 있다")
    void deleteMenuSuccessTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);
        MenuEntity menu = createMenuEntity(menuId,"menu1", 10000,"menu1's des", store);

        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.of(menu));

        menuServiceV1.deleteMenu(storeId,menuId,authUser);
        assertThat(menu.getIsDeleted()).isTrue();
    }

    @Test
    @DisplayName("[성공] 설명 없음 +  AI false + 프롬포트 없으면 설명 null로 메뉴 생성")
    void addMenuSuccessDescriptionNullTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        UserEntity owner = createOwnerEntity(ownerId);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request, "description", null);
        ReflectionTestUtils.setField(request, "aiDescription", false);
        ReflectionTestUtils.setField(request, "aiPrompt", null);

        MenuEntity menu = createMenuEntity(
                menuId,
                request.getName(),
                request.getPrice(),
                null,
                store);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(false);
        given(menuRepository.save(any(MenuEntity.class))).willReturn(menu);

        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId, request,authUser);

        assertThat(response).isNotNull();
        assertThat(response.getDescription()).isNull();
    }

    @Test
    @DisplayName("[실패] AI false + 프롬포트 있음 >> 예외 처리")
    void addMenuFailAiPromptNotAllowedTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request, "description", null);
        ReflectionTestUtils.setField(request, "aiDescription", false);
        ReflectionTestUtils.setField(request, "aiPrompt", "맛있게 설명 작성해줘");

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> menuServiceV1.addMenu(storeId, request,authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_AI_PROMPT_NOT_ALLOWED);
    }

    @Test
    @DisplayName("[실패] 설명 + AI true >> 예외 처리")
    void addMenuFailDescriptionDuplicatedTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        UserEntity owner = createOwnerEntity(ownerId);
        AuthUser authUser = createAuthUser(ownerId,UserRole.OWNER);
        StoreEntity store = createStoreEntity(storeId,"test store", owner);

        ReqCreateMenuDtoV1 request = createReqCreateMenuDtoV1();
        ReflectionTestUtils.setField(request, "description", "일반 설명");
        ReflectionTestUtils.setField(request, "aiDescription", true);
        ReflectionTestUtils.setField(request, "aiPrompt", null);

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, request.getName())).willReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> menuServiceV1.addMenu(storeId, request,authUser));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MENU_DESCRIPTION_DUPLICATE);
    }
}
