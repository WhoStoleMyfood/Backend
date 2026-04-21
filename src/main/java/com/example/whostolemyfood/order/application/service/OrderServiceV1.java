package com.example.whostolemyfood.order.application.service;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public ResCreateOrderDtoV1 createOrder(ReqCreateOrderDtoV1 request) {
        // TODO: [인증/인가] SecurityContext 기반 CUSTOMER ID 추출
        UUID mockUserId = UUID.randomUUID(); 

        int totalPrice = request.getOrderItems().stream()
                .mapToInt(item -> item.getPriceAtOrder() * item.getQuantity())
                .sum();

        OrderEntity order = OrderEntity.builder()
                .userId(mockUserId)
                .storeId(request.getStoreId())
                .addressId(request.getAddressId())
                .request(request.getRequest())
                .totalPrice(totalPrice)
                .deliveryFee(3000) 
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItemEntity> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItemEntity.builder()
                        .order(order)
                        .menuId(itemRequest.getMenuId())
                        .quantity(itemRequest.getQuantity())
                        .priceAtOrder(itemRequest.getPriceAtOrder())
                        .build())
                .toList(); // .collect(Collectors.toList())에서 변경

        order.getOrderItems().addAll(orderItems);

        OrderEntity savedOrder = orderRepository.save(order);
        return ResCreateOrderDtoV1.from(savedOrder);
    }

    /**
     * 주문 단건 조회
     */
    public ResGetOrderDtoV1 getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. ID: " + orderId));
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 목록 조회 (페이징 및 검색)
     * - storeId: 특정 가게 필터
     * - isHidden: 숨김 여부 필터
     */
    public Page<ResGetOrderListDtoV1> getOrders(UUID storeId, Boolean isHidden, Pageable pageable) {
        if (storeId != null && isHidden != null) {
            return orderRepository.findAllByStoreIdAndIsHidden(storeId, isHidden, pageable).map(ResGetOrderListDtoV1::from);
        } else if (storeId != null) {
            return orderRepository.findAllByStoreId(storeId, pageable).map(ResGetOrderListDtoV1::from);
        } else if (isHidden != null) {
            return orderRepository.findAllByIsHidden(isHidden, pageable).map(ResGetOrderListDtoV1::from);
        }
        return orderRepository.findAll(pageable).map(ResGetOrderListDtoV1::from);
    }

    /**
     * 주문 취소
     */
    @Transactional
    public ResGetOrderDtoV1 cancelOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(order.getCreatedAt(), now);
        if (duration.toMinutes() >= 5) {
            throw new IllegalStateException("주문 생성 후 5분이 경과하여 취소할 수 없습니다.");
        }

        order.cancelOrder();
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 수정(요청사항 수정)
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderRequest(UUID orderId, String newRequest) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 상태 검증 로직은 OrderEntity.updateRequest 내부로 위임하여 중복 제거
        order.updateRequest(newRequest); 
        
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        order.markAsDeleted(UUID.randomUUID()); 
    }

    /**
     * 주문 상태 변경 (사장님/관리자용)
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderStatus(UUID orderId, OrderStatus nextStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        
        // TODO: [인가] OWNER, MANAGER 권한 및 상태 흐름 검증 로직 추가
        order.updateStatus(nextStatus);
        
        return ResGetOrderDtoV1.from(order);
    }
}
