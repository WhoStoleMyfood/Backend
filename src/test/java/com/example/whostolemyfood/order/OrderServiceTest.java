package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
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
        OrderEntity order = OrderEntity.builder().userId(OrderServiceV1.MOCK_USER_ID).status(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order, "orderId", orderId); // 🚨 빌더 대신 Reflection 사용
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now().minusMinutes(2));
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.cancelOrder(orderId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("[성공] 사장님이 주문 상태를 순차적으로 변경할 수 있어야 함")
    void updateOrderStatusSuccessTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().status(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("[성공] 활성 상태인 주문은 상세 조회가 정상적으로 수행되어야 함")
    void getOrderSuccessTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().totalPrice(23000).deliveryFee(3000).build();
        ReflectionTestUtils.setField(order, "orderId", orderId);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ResGetOrderDtoV1 response = orderService.getOrder(orderId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("[성공] 주문 목록 조회 시 페이징 데이터가 정상적으로 반환되어야 함")
    void getOrdersSuccessTest() {
        OrderEntity order = OrderEntity.builder().totalPrice(20000).build();
        ReflectionTestUtils.setField(order, "orderId", UUID.randomUUID());
        
        Page<OrderEntity> page = new PageImpl<>(List.of(order));
        given(orderRepository.findAllByIsDeletedFalse(any())).willReturn(page);

        Page<ResGetOrderListDtoV1> result = orderService.getOrders(null, null, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[실패] 주문 생성 후 5분이 경과하면 취소가 불가능해야 함 (ORDER_CANCEL_TIME_EXCEEDED)")
    void cancelOrderTimeLimitTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity oldOrder = OrderEntity.builder().status(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(oldOrder, "orderId", orderId);
        ReflectionTestUtils.setField(oldOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(oldOrder));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.cancelOrder(orderId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
    }

    @Test
    @DisplayName("[실패] 이미 수락된 주문은 취소가 불가능해야 함 (ORDER_CANCEL_NOT_PENDING)")
    void cancelOrderNotPendingTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity acceptedOrder = OrderEntity.builder().status(OrderStatus.ACCEPTED).build();
        ReflectionTestUtils.setField(acceptedOrder, "orderId", orderId);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(acceptedOrder));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.cancelOrder(orderId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANCEL_NOT_PENDING);
    }

    @Test
    @DisplayName("[실패] 이미 최종 상태에 도달한 주문은 상태 변경 불가 (ORDER_ALREADY_FINALIZED)")
    void updateOrderStatusFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity completedOrder = OrderEntity.builder().status(OrderStatus.COMPLETED).build();
        ReflectionTestUtils.setField(completedOrder, "orderId", orderId);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(completedOrder));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.updateOrderStatus(orderId, OrderStatus.ACCEPTED));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_ALREADY_FINALIZED);
    }

    @Test
    @DisplayName("[실패] 배달 완료된 주문은 삭제 불가 (ORDER_CANNOT_DELETE_DELIVERED)")
    void deleteOrderFailureTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity deliveredOrder = OrderEntity.builder().status(OrderStatus.DELIVERED).build();
        ReflectionTestUtils.setField(deliveredOrder, "orderId", orderId);
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(deliveredOrder));

        CustomException exception = assertThrows(CustomException.class, () -> orderService.deleteOrder(orderId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ORDER_CANNOT_DELETE_DELIVERED);
    }
}