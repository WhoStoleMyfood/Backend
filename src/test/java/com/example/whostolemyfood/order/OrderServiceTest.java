package com.example.whostolemyfood.order;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderServiceV1 orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private AddressRepository addressRepository;

    @Test
    @DisplayName("[성공] 주문 생성 - 모든 운영 정책(영업시간, 최소금액 등) 통과 시 성공")
    void createOrderSuccessTest() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(storeId).addressId(addressId)
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder()
                        .menuId(menuId).quantity(2).priceAtOrder(10000).build()))
                .build();

        // 가게 모킹 (영업중, 24시간, 최소금액 5000원)
        StoreEntity store = StoreEntity.builder()
                .status(StoreStatus.OPEN)
                .openTime(LocalTime.MIN)
                .closeTime(LocalTime.MAX)
                .minOrderPrice(5000)
                .isHidden(false)
                .build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        AddressEntity address = AddressEntity.builder().userId(userId).build();
        given(addressRepository.findByIdAndIsDeletedFalse(addressId)).willReturn(Optional.of(address));

        MenuEntity menu = MenuEntity.builder().price(10000).build();
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        given(orderRepository.save(any())).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());
            return order;
        });

        ResCreateOrderDtoV1 response = orderService.createOrder(request, userId);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("[실패] 주문 생성 - 최소 주문 금액 미달 시 실패 (ORDER_MIN_PRICE_NOT_MET)")
    void createOrderMinPriceFailTest() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        
        // 음식 총액 2000원 요청
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(storeId).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder()
                        .menuId(UUID.randomUUID()).quantity(1).priceAtOrder(2000).build()))
                .build();

        // 가게 최소 주문 금액은 15000원
        StoreEntity store = StoreEntity.builder()
                .status(StoreStatus.OPEN)
                .openTime(LocalTime.MIN).closeTime(LocalTime.MAX)
                .minOrderPrice(15000)
                .build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        
        // 배송지 및 메뉴 모킹 (간소화)
        given(addressRepository.findByIdAndIsDeletedFalse(any())).willReturn(Optional.of(AddressEntity.builder().userId(userId).build()));
        given(menuRepository.findById(any())).willReturn(Optional.of(MenuEntity.builder().price(2000).build()));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_MIN_PRICE_NOT_MET);
    }

    @Test
    @DisplayName("[실패] 주문 생성 - 영업 시간이 아닐 경우 실패 (STORE_CLOSED)")
    void createOrderTimeFailTest() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder().storeId(storeId).build();

        // 가게 영업 시간 종료 (이미 지난 시간으로 세팅)
        StoreEntity store = StoreEntity.builder()
                .status(StoreStatus.OPEN)
                .openTime(LocalTime.of(0, 0))
                .closeTime(LocalTime.of(0, 1)) 
                .build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STORE_CLOSED);
    }

    @Test
    @DisplayName("[실패] 주문 생성 - 클라이언트 가격 조작 방어 (PRICE_MISMATCH)")
    void createOrderPriceMismatchTest() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();

        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(storeId).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder()
                        .menuId(menuId).quantity(1).priceAtOrder(500).build())) // 조작 시도
                .build();

        StoreEntity store = StoreEntity.builder().status(StoreStatus.OPEN).openTime(LocalTime.MIN).closeTime(LocalTime.MAX).build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(addressRepository.findByIdAndIsDeletedFalse(any())).willReturn(Optional.of(AddressEntity.builder().userId(userId).build()));

        MenuEntity menu = MenuEntity.builder().price(10000).build(); // 진짜 가격은 만원
        ReflectionTestUtils.setField(menu, "menuId", menuId);
        given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.createOrder(request, userId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRICE_MISMATCH);
    }

    @Test
    @DisplayName("[성공] 권한 제어 - CUSTOMER는 본인의 주문만 상세 조회 가능")
    void getOrderSuccessTest() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().userId(userId).build();
        ReflectionTestUtils.setField(order, "orderId", orderId);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.getOrder(orderId, userId, UserRole.CUSTOMER);
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("[실패] 권한 제어 - CUSTOMER가 타인의 주문을 조회하면 실패 (ORDER_NOT_OWNER)")
    void getOrderFailTest() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().userId(otherUserId).build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.getOrder(orderId, userId, UserRole.CUSTOMER));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_NOT_OWNER);
    }

    @Test
    @DisplayName("[성공] 주문 취소 - 본인 주문이며 5분 이내일 때 성공")
    void cancelOrderSuccessTest() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().userId(userId).status(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.cancelOrder(orderId, userId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("[실패] 권한 제어 - CUSTOMER는 주문 상태 변경 불가 (ACCESS_DENIED)")
    void updateOrderStatusDeniedTest() {
        UUID userId = UUID.randomUUID();
        given(orderRepository.findById(any())).willReturn(Optional.of(OrderEntity.builder().status(OrderStatus.PENDING).build()));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.updateOrderStatus(UUID.randomUUID(), OrderStatus.ACCEPTED, userId, UserRole.CUSTOMER));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("[성공] 권한 제어 - MANAGER는 모든 주문 삭제 가능")
    void deleteOrderSuccessTest() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().status(OrderStatus.PENDING).build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        orderService.deleteOrder(orderId, adminId, UserRole.MANAGER);
        // softDelete가 내부적으로 호출됨
    }
}
