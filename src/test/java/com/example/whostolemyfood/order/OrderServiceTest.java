package com.example.whostolemyfood.order;

import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @DisplayName("주문 생성 시 서버에서 상품별 총 결제 금액을 정확히 계산해야 함")
    void createOrderPriceCalculationTest() {
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .orderItems(List.of(
                        ReqCreateOrderDtoV1.OrderItemRequest.builder().priceAtOrder(20000).quantity(1).build(),
                        ReqCreateOrderDtoV1.OrderItemRequest.builder().priceAtOrder(5000).quantity(2).build()
                ))
                .build();

        given(orderRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ResCreateOrderDtoV1 response = orderService.createOrder(request);

        // 20000*1 + 5000*2 = 30000
        assertThat(response.getTotalPrice()).isEqualTo(30000);
    }

    @Test
    @DisplayName("주문 생성 후 5분이 경과하면 취소가 불가능해야 함 (IllegalStateException)")
    void cancelOrderTimeLimitTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity oldOrder = OrderEntity.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();
        
        // 생성 시간을 6분 전으로 강제 설정
        ReflectionTestUtils.setField(oldOrder, "createdAt", LocalDateTime.now().minusMinutes(6));
        
        given(orderRepository.findById(orderId)).willReturn(Optional.of(oldOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(orderId));
    }

    @Test
    @DisplayName("수락된 주문(ACCEPTED)은 요청사항을 수정할 수 없어야 함 (도메인 보호 로직)")
    void updateOrderRequestStatusCheckTest() {
        UUID orderId = UUID.randomUUID();
        OrderEntity acceptedOrder = OrderEntity.builder()
                .orderId(orderId)
                .status(OrderStatus.ACCEPTED)
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(acceptedOrder));

        assertThrows(IllegalStateException.class, () -> orderService.updateOrderRequest(orderId, "수정요청"));
    }
}
