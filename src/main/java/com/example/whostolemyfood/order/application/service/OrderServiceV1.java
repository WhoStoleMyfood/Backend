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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    // TODO: [미래대비] 타 도메인 연동을 위한 레포지토리 미리 선언
    // private final StoreRepository storeRepository;
    // private final AddressRepository addressRepository;

    /**
     * 주문 생성 (음식값 + 배달비 합산 로직 포함)
     */
    @Transactional
    public ResCreateOrderDtoV1 createOrder(ReqCreateOrderDtoV1 request) {
        // TODO: [인증/인가] SecurityContext 기반 CUSTOMER ID 추출
        UUID mockUserId = UUID.randomUUID(); 

        // TODO: [미래대비] Store, Address 존재 여부 검증 로직이 들어올 자리
        // storeRepository.findById(request.getStoreId()).orElseThrow(...);
        // addressRepository.findById(request.getAddressId()).orElseThrow(...);

        int itemTotalPrice = request.getOrderItems().stream()
                .mapToInt(item -> item.getPriceAtOrder() * item.getQuantity())
                .sum();

        int deliveryFee = 3000; 
        int finalTotalPrice = itemTotalPrice + deliveryFee;

        OrderEntity order = OrderEntity.builder()
                .userId(mockUserId)
                .storeId(request.getStoreId())
                .addressId(request.getAddressId())
                .request(request.getRequest())
                .totalPrice(finalTotalPrice)
                .deliveryFee(deliveryFee)
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItemEntity> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItemEntity.builder()
                        .order(order)
                        .menuId(itemRequest.getMenuId())
                        .quantity(itemRequest.getQuantity())
                        .priceAtOrder(itemRequest.getPriceAtOrder())
                        .build())
                .toList();

        order.getOrderItems().addAll(orderItems);

        OrderEntity savedOrder = orderRepository.save(order);
        return ResCreateOrderDtoV1.from(savedOrder);
    }

    /**
     * 주문 단건 조회 (삭제된 주문은 필터링)
     */
    public ResGetOrderDtoV1 getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 주문입니다. ID: " + orderId));
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 목록 조회 (페이징 및 검색 - 삭제된 데이터 제외)
     */
    public Page<ResGetOrderListDtoV1> getOrders(UUID storeId, Boolean isHidden, Pageable pageable) {
        if (storeId != null && isHidden != null) {
            return orderRepository.findAllByStoreIdAndIsHiddenAndIsDeletedFalse(storeId, isHidden, pageable).map(ResGetOrderListDtoV1::from);
        } else if (storeId != null) {
            return orderRepository.findAllByStoreIdAndIsDeletedFalse(storeId, pageable).map(ResGetOrderListDtoV1::from);
        } else if (isHidden != null) {
            return orderRepository.findAllByIsHiddenAndIsDeletedFalse(isHidden, pageable).map(ResGetOrderListDtoV1::from);
        }
        return orderRepository.findAllByIsDeletedFalse(pageable).map(ResGetOrderListDtoV1::from);
    }

    /**
     * 주문 취소 (5분 제한 및 삭제 상태 체크)
     */
    @Transactional
    public ResGetOrderDtoV1 cancelOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없거나 이미 삭제되었습니다."));

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(order.getCreatedAt(), now);
        if (duration.toMinutes() >= 5) {
            throw new IllegalStateException("주문 생성 후 5분이 경과하여 취소할 수 없습니다.");
        }

        order.cancelOrder();
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 수정(요청사항 수정 - PENDING 상태만 가능)
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderRequest(UUID orderId, String newRequest) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없거나 이미 삭제되었습니다."));

        order.updateRequest(newRequest); 
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 상태 변경 (사장님/관리자용)
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderStatus(UUID orderId, OrderStatus nextStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없거나 이미 삭제되었습니다."));
        
        // [비즈니스 검증] 이미 취소되거나 완료된 주문은 상태 변경 불가
        if (order.getStatus() == OrderStatus.CANCELLED || 
            order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("이미 최종 상태(취소/완료)에 도달한 주문은 상태를 변경할 수 없습니다.");
        }

        order.updateStatus(nextStatus);
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 삭제 (Soft Delete 및 비즈니스 검증)
     */
    @Transactional
    public void deleteOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new IllegalArgumentException("이미 삭제되었거나 존재하지 않는 주문입니다."));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("배달이 완료된 주문은 삭제할 수 없습니다.");
        }

        order.markAsDeleted(UUID.randomUUID()); 
    }
}
