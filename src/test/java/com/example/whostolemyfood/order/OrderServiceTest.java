package com.example.whostolemyfood.order;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
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
    @DisplayName("[성공] 주문 생성 - CUSTOMER 권한으로 모든 검증 통과")
    void createOrderSuccessTest() {
        UUID userId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(storeId).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder()
                        .menuId(UUID.randomUUID()).quantity(2).priceAtOrder(10000).build()))
                .build();

        StoreEntity store = StoreEntity.builder().status(StoreStatus.OPEN).openTime(LocalTime.MIN).closeTime(LocalTime.MAX).minOrderPrice(5000).build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(addressRepository.findByIdAndIsDeletedFalse(any())).willReturn(Optional.of(AddressEntity.builder().userId(userId).build()));
        given(menuRepository.findById(any())).willReturn(Optional.of(MenuEntity.builder().price(10000).build()));

        given(orderRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ResCreateOrderDtoV1 response = orderService.createOrder(request, userId, UserRole.CUSTOMER);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("[실패] 주문 생성 - OWNER 권한으로 주문 시도 시 차단 (SA 준수)")
    void createOrderFailByRoleTest() {
        CustomException ex = assertThrows(CustomException.class, 
                () -> orderService.createOrder(ReqCreateOrderDtoV1.builder().build(), UUID.randomUUID(), UserRole.OWNER));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("[성공] 상태 변경 - OWNER가 본인 가게 주문을 순차적으로 변경 (PENDING -> ACCEPTED)")
    void updateOrderStatusSuccessByOwnerTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().storeId(storeId).status(OrderStatus.PENDING).build();
        given(orderRepository.findById(any())).willReturn(Optional.of(order));

        UserEntity owner = UserEntity.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        StoreEntity store = StoreEntity.builder().user(owner).build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        ResGetOrderDtoV1 response = orderService.updateOrderStatus(UUID.randomUUID(), OrderStatus.ACCEPTED, ownerId, UserRole.OWNER);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("[실패] 상태 변경 - OWNER가 순서를 건너뛰려 할 때 차단 (ACCEPTED -> DELIVERED)")
    void updateOrderStatusJumpFailByOwnerTest() {
        UUID ownerId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().storeId(storeId).status(OrderStatus.ACCEPTED).build(); // 현재 수락됨
        given(orderRepository.findById(any())).willReturn(Optional.of(order));

        UserEntity owner = UserEntity.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);
        given(storeRepository.findById(storeId)).willReturn(Optional.of(StoreEntity.builder().user(owner).build()));

        // 사장님은 ACCEPTED 다음에 바로 DELIVERED로 갈 수 없음 (중간 단계 누락)
        assertThrows(CustomException.class, 
                () -> orderService.updateOrderStatus(UUID.randomUUID(), OrderStatus.DELIVERED, ownerId, UserRole.OWNER));
    }

    @Test
    @DisplayName("[성공] 상태 변경 - MASTER 권한은 모든 순서를 무시하고 강제 변경 가능 (슈퍼 권한)")
    void updateOrderStatusForceByMasterTest() {
        OrderEntity order = OrderEntity.builder().status(OrderStatus.PENDING).build(); // 현재 대기중
        given(orderRepository.findById(any())).willReturn(Optional.of(order));

        // MASTER는 PENDING에서 바로 COMPLETED로 점프 가능
        ResGetOrderDtoV1 response = orderService.updateOrderStatus(UUID.randomUUID(), OrderStatus.COMPLETED, UUID.randomUUID(), UserRole.MASTER);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("[성공] 주문 취소 - MASTER는 5분 제한 없이 강제 취소 가능 (SA 준수)")
    void cancelOrderByMasterTest() {
        OrderEntity oldOrder = OrderEntity.builder().status(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(oldOrder, "createdAt", LocalDateTime.now().minusMinutes(10)); // 10분 전 주문
        given(orderRepository.findById(any())).willReturn(Optional.of(oldOrder));

        ResGetOrderDtoV1 response = orderService.cancelOrder(UUID.randomUUID(), UUID.randomUUID(), UserRole.MASTER);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("[실패] 주문 삭제 - 오직 MASTER만 가능하며 MANAGER는 거부됨 (SA 준수)")
    void deleteOrderFailByManagerTest() {
        CustomException ex = assertThrows(CustomException.class, 
                () -> orderService.deleteOrder(UUID.randomUUID(), UUID.randomUUID(), UserRole.MANAGER));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }
}
