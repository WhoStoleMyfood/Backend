package com.example.whostolemyfood.order;

import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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
    private Object storeRepository; 
    
    @Mock
    private Object addressRepository;

    @Test
    @DisplayName("[성공] 주문 생성 시 음식값과 배달비(3000원)가 정확히 합산되어야 함")
    void createOrderPriceCalculationTest() {
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder().priceAtOrder(20000).quantity(1).build()))
                .build();
        
        given(orderRepository.save(any())).willAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());
            return order;
        });

        ResCreateOrderDtoV1 response = orderService.createOrder(request);
        assertThat(response.getTotalPrice()).isEqualTo(23000);
        assertThat(response.getOrderId()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 5분 이내 취소 요청 시 주문 상태가 CANCELLED로 변경되어야 함")
    void cancelOrderSuccessTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().orderId(orderId).status(OrderStatus.PENDING).isDeleted(false).build();
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(2));
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.cancelOrder(orderId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("[성공] 사장님이 주문 상태를 ACCEPTED로 변경할 수 있어야 함")
    void updateOrderStatusSuccessTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().orderId(orderId).status(OrderStatus.PENDING).isDeleted(false).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("[성공] 활성 상태인 주문은 상세 조회가 정상적으로 수행되어야 함")
    void getOrderSuccessTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().orderId(orderId).totalPrice(23000).deliveryFee(3000).isDeleted(false).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.getOrder(orderId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("[성공] 주문 목록 조회 시 페이징 데이터가 정상적으로 반환되어야 함")
    void getOrdersSuccessTest() {
        OrderEntity order = OrderEntity.builder().orderId(UUID.randomUUID()).totalPrice(20000).isDeleted(false).build();
        Page<OrderEntity> page = new PageImpl<>(List.of(order));
        given(orderRepository.findAllByIsDeletedFalse(any())).willReturn(page);

        Page<ResGetOrderListDtoV1> result = orderService.getOrders(null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[실패] 주문 생성 후 5분이 경과하면 취소가 불가능해야 함")
    void cancelOrderTimeLimitTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity oldOrder = OrderEntity.builder().orderId(orderId).status(OrderStatus.PENDING).isDeleted(false).build();
        ReflectionTestUtils.setField(oldOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        given(orderRepository.findById(orderId)).willReturn(Optional.of(oldOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    @DisplayName("[실패] 이미 취소된(CANCELLED) 주문은 상태를 변경할 수 없어야 함")
    void updateOrderStatusCancelledFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity cancelledOrder = OrderEntity.builder().orderId(orderId).status(OrderStatus.CANCELLED).isDeleted(false).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(cancelledOrder));

        assertThrows(IllegalStateException.class, () -> orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED));
    }

    @Test
    @DisplayName("[실패] 이미 삭제된(isDeleted=true) 주문은 상세 조회가 불가능해야 함")
    void getOrderDeletedFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity deletedOrder = OrderEntity.builder().orderId(orderId).isDeleted(true).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(deletedOrder));

        assertThrows(IllegalArgumentException.class, () -> orderService.getOrder(orderId));
    }

    @Test
    @DisplayName("[실패] 이미 수락(ACCEPTED)된 주문은 요청사항을 수정할 수 없어야 함")
    void updateOrderRequestFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity acceptedOrder = OrderEntity.builder().orderId(orderId).status(OrderStatus.ACCEPTED).isDeleted(false).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(acceptedOrder));

        assertThrows(IllegalStateException.class, () -> orderService.updateOrderRequest(orderId, "수정해주세요"));
    }

    @Test
    @DisplayName("[실패] 배달 완료(DELIVERED)된 주문은 삭제할 수 없어야 함")
    void deleteOrderDeliveredFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity deliveredOrder = OrderEntity.builder().orderId(orderId).status(OrderStatus.DELIVERED).isDeleted(false).build();
        given(orderRepository.findById(orderId)).willReturn(Optional.of(deliveredOrder));

        assertThrows(IllegalStateException.class, () -> orderService.deleteOrder(orderId));
    }
}
