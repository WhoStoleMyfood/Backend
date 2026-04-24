//package com.example.whostolemyfood.store;
//
//import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
//import com.example.whostolemyfood.order.domain.repository.OrderRepository;
//import com.example.whostolemyfood.store.application.service.StoreServiceV1;
//import com.example.whostolemyfood.store.domain.entity.StoreEntity;
//import com.example.whostolemyfood.store.domain.entity.StoreStatus;
//import com.example.whostolemyfood.store.domain.repository.StoreRepository;
//import com.example.whostolemyfood.store.presentation.dto.request.ReqCreateStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.request.ReqUpdateStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.response.ResCreateStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreDtoV1;
//import com.example.whostolemyfood.store.presentation.dto.response.ResGetStoreListDtoV1;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//
//@ExtendWith(MockitoExtension.class)
//public class StoreServiceV1Test {
//
//    @InjectMocks
//    private StoreServiceV1 storeServiceV1;
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    // 테스트용 생성값
//    private ReqCreateStoreDtoV1 createStoreRequest() {
//        ReqCreateStoreDtoV1 request = new ReqCreateStoreDtoV1();
//        ReflectionTestUtils.setField(request, "name", "스파르닭");
//        ReflectionTestUtils.setField(request, "address", "내일시 배움구 캠프 5동");
//        ReflectionTestUtils.setField(request, "phone", "000-0000-0000");
//        ReflectionTestUtils.setField(request, "content", "This is spar닭");
//        ReflectionTestUtils.setField(request, "minOrderPrice", 23000);
//        ReflectionTestUtils.setField(request, "openTime", LocalTime.of(10, 0));
//        ReflectionTestUtils.setField(request, "closeTime", LocalTime.of(23, 0));
//        return request;
//    }
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
//    @Test
//    @DisplayName("[성공] 스토어 생성 시 요청값으로 생성이 되며 DTO로 반환되어야 한다")
//    void createStoreSuccessTest() {
//        // given
//        ReqCreateStoreDtoV1 request = createStoreRequest();
//        UUID storeId = UUID.randomUUID();
//
//        given(storeRepository.existsByNameAndIsDeletedFalse("스파르닭"))
//                .willReturn(false);
//
//        given(storeRepository.save(any(StoreEntity.class)))
//                .willAnswer(invocation -> {
//                    StoreEntity store = invocation.getArgument(0);
//                    ReflectionTestUtils.setField(store,"storeId", storeId);
//                    return store;
//                });
//        // when
//        ResCreateStoreDtoV1 response = storeServiceV1.createStore(request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getName()).isEqualTo("스파르닭");
//        assertThat(response.getAddress()).isEqualTo("내일시 배움구 캠프 5동");
//        assertThat(response.getPhone()).isEqualTo("000-0000-0000");
//        assertThat(response.getContent()).isEqualTo("This is spar닭");
//        assertThat(response.getMinOrderPrice()).isEqualTo(23000);
//        assertThat(response.getStoreId()).isNotNull();
//    }
//
//    @Test
//    @DisplayName("[실패] 이미 같은 이름의 스토어가 존재하면 생성할 수 없다")
//    void createStoreFailTest() {
//        // given
//        ReqCreateStoreDtoV1 request = createStoreRequest();
//
//        given(storeRepository.existsByNameAndIsDeletedFalse("스파르닭"))
//        .willReturn(true);
//        // when & then
//        assertThrows(IllegalArgumentException.class, ()-> storeServiceV1.createStore(request));
//    }
//
//    @Test
//    @DisplayName("[성공] 스토어 조회시 DTO로 반환")
//    void getStoreSuccessTest() {
//        UUID storeId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//
//        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId)).willReturn(Optional.of(store));
//
//        ResGetStoreDtoV1 response = storeServiceV1.getStore(storeId);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStoreId()).isEqualTo(storeId);
//        assertThat(response.getName()).isEqualTo(store.getName());
//        assertThat(response.getAddress()).isEqualTo(store.getAddress());
//        assertThat(response.getPhone()).isEqualTo(store.getPhone());
//        assertThat(response.getContent()).isEqualTo(store.getContent());
//        assertThat(response.getMinOrderPrice()).isEqualTo(store.getMinOrderPrice());
//        assertThat(response.getOpenTime()).isEqualTo(store.getOpenTime());
//        assertThat(response.getCloseTime()).isEqualTo(store.getCloseTime());
//    }
//
//    @Test
//    @DisplayName("[실패] 존재하지 않는 스토어 조회 시 예외 발생")
//    void getStoreFailTest() {
//        UUID storeId = UUID.randomUUID();
//
//        given(storeRepository.findByStoreIdAndIsHiddenFalseAndIsDeletedFalse(storeId)).willReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, ()-> storeServiceV1.getStore(storeId));
//    }
//
//    @Test
//    @DisplayName("[성공] 목록 조회시 페이징 데이터가 DTO를 통해 반환")
//    void getStoresSuccessTest() {
//        UUID storeId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<StoreEntity> stores = new PageImpl<>(List.of(store), pageable, 1);
//
//        given(storeRepository.findAllByIsHiddenFalseAndIsDeletedFalse(any(Pageable.class))).willReturn(stores);
//
//       Page<ResGetStoreListDtoV1> response = storeServiceV1.getStores(pageable);
//
//       assertThat(response).isNotNull();
//       assertThat(response.getTotalElements()).isEqualTo(1);
//       assertThat(response.getContent()).hasSize(1);
//       assertThat(response.getContent().get(0).getStoreId()).isEqualTo(storeId);
//       assertThat(response.getContent().get(0).getName()).isEqualTo(store.getName());
//    }
//
//
//    @Test
//    @DisplayName("[실패] 삭제 처리된 스토어는 수정할 수 없다")
//    void isDeletedStoreDeleteFailTest() {
//        UUID storeId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//
//        ReflectionTestUtils.setField(store,"isDeleted", true);
//
//        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, ()-> storeServiceV1.deleteStore(storeId));
//    }
//
//    @Test
//    @DisplayName("[실패] 삭제 처리된 스토어는 숨김 / 노출 할 수 없다")
//    void hiddenStoreDeleteFailTest() {
//        UUID storeId = UUID.randomUUID();
//        StoreEntity store = createStoreEntity(storeId);
//        ReflectionTestUtils.setField(store,"isDeleted", true);
//
//        given(storeRepository.findByStoreIdAndIsDeletedFalse(storeId)).willReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class, ()-> storeServiceV1.hiddenStore(storeId));
//    }
//}
