//package com.example.whostolemyfood.menu;
//
//import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
//import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
//import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
//import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
//import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
//import com.example.whostolemyfood.store.domain.entity.StoreEntity;
//import com.example.whostolemyfood.store.domain.entity.StoreStatus;
//import com.example.whostolemyfood.store.domain.repository.StoreRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//public class MenuServiceTest {
//
//    @InjectMocks
//    private MenuServiceV1 menuServiceV1;
//
//    @Mock
//    private MenuRepository menuRepository;
//    @Mock
//    private StoreRepository storeRepository;
//
//    // 테스트용 생성값
//    private ReqCreateMenuDtoV1 createMenuRequest() {
//        ReqCreateMenuDtoV1 request = new ReqCreateMenuDtoV1();
//        ReflectionTestUtils.setField(request, "name", "올리브유 구덩이에 빠진 스파르닭");
//        ReflectionTestUtils.setField(request, "price", 30000);
//        ReflectionTestUtils.setField(request, "description", "this is sparta");
//        return request;
//    };
//
//    private StoreEntity createStoreEntity(UUID storeId) {
//        StoreEntity store = StoreEntity.builder()
//                .name("스파르닭")
//                .address("내일시 배움구 캠프 5동")
//                .phone("000-0000-0000")
//                .content("This is spar닭")
//                .minOrderPrice(23000)
//                .status(StoreStatus.OPEN)
//                .openTime(LocalTime.of(10, 0))
//                .closeTime(LocalTime.of(23, 0))
//                .build();
//
//        ReflectionTestUtils.setField(store, "storeId", storeId);
//        ReflectionTestUtils.setField(store, "isHidden", false);
//        ReflectionTestUtils.setField(store, "isDeleted", false);
//
//        return store;
//    }
//
//    private MenuEntity createMenuEntity(UUID menuId, StoreEntity store) {
//        MenuEntity menu = MenuEntity.builder()
//                .store(store)
//                .name("올리브유 구덩이에 빠진 스파르닭")
//                .price(30000)
//                .description("this is sparta")
//                .build();
//
//        ReflectionTestUtils.setField(menu, "menuId", menuId);
//        ReflectionTestUtils.setField(menu, "isHidden", false);
//        ReflectionTestUtils.setField(menu, "isDeleted", false);
//
//        return menu;
//    }
//
//    @Test
//    @DisplayName("[성공] 메뉴를 생성하여 DTO로 반환")
//    void addMenuSuccessTest() {
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//
//        ReqCreateMenuDtoV1 request = createMenuRequest();
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(menuId, store);
//
//        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
//        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, menu.getName())).willReturn(false);
//        given(menuRepository.save(any(MenuEntity.class))).willReturn(menu);
//
//        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(storeId,request);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStoreId()).isEqualTo(storeId);
//        assertThat(response.getName()).isEqualTo(menu.getName());
//        assertThat(response.getPrice()).isEqualTo(request.getPrice());
//        assertThat(response.getDescription()).isEqualTo(menu.getDescription());
//    }
//
//    @Test
//    @DisplayName("[실패] 같은 이름의 메뉴가 스토어에 존재한다면 추가할 수 없다")
//    void addMenuFailTest() {
//        UUID storeId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//
//        ReqCreateMenuDtoV1 request = createMenuRequest();
//
//        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
//        given(menuRepository.existsByStore_StoreIdAndNameAndIsDeletedFalse(storeId, "올리브유 구덩이에 빠진 스파르닭")).willReturn(true);
//
//        assertThrows(IllegalArgumentException.class, () -> menuServiceV1.addMenu(storeId, request));
//    }
//
//    @Test
//    @DisplayName("[실패] 숨겨진 메뉴는 조회할 수 없다")
//    void isHiddenMenuGetFailTest() {
//        UUID storeId = UUID.randomUUID();
//        UUID menuId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//        MenuEntity menu = createMenuEntity(menuId, store);
//
//        ReflectionTestUtils.setField(menu, "isHidden", true);
//
//        given(menuRepository.findByMenuIdAndStore_StoreIdAndIsHiddenFalseAndIsDeletedFalse(menuId, storeId)).willReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, ()-> menuServiceV1.getMenu(storeId, menuId));
//    }
//
//
//}
