package com.example.whostolemyfood.order.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    public static final UUID MOCK_USER_ID = UUID.fromString("a7a4e42a-e45a-450c-bcee-ff05c4235698");

    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public ResCreateOrderDtoV1 createOrder(ReqCreateOrderDtoV1 request) {
        UUID currentUserId = MOCK_USER_ID; 

        int itemTotalPrice = request.getOrderItems().stream()
                .mapToInt(item -> item.getPriceAtOrder() * item.getQuantity())
                .sum();

        int deliveryFee = 3000; 
        int finalTotalPrice = itemTotalPrice + deliveryFee;

        if (finalTotalPrice <= 0) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        OrderEntity order = OrderEntity.builder()
                .userId(currentUserId)
                .storeId(request.getStoreId())
                .addressId(request.getAddressId())
                .request(request.getRequest())
                .totalPrice(finalTotalPrice)
                .deliveryFee(deliveryFee)
                .status(OrderStatus.PENDING)
                .build();
        
        // [Audit 필수 요구사항 준수] 데이터 생성자 기록
        order.markCreatedBy(currentUserId);

        List<OrderItemEntity> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItemEntity.builder()
                        .order(order)
                        .menuId(itemRequest.getMenuId())
                        .quantity(itemRequest.getQuantity())
                        .priceAtOrder(itemRequest.getPriceAtOrder())
                        .createdBy(currentUserId)
                        .build())
                .toList();

        order.getOrderItems().addAll(orderItems);

        OrderEntity savedOrder = orderRepository.save(order);
        return ResCreateOrderDtoV1.from(savedOrder, "주문이 성공적으로 생성되었습니다.");
    }

    /**
     * 주문 단건 조회
     */
    public ResGetOrderDtoV1 getOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 목록 조회
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
     * 주문 취소
     */
    @Transactional
    public ResGetOrderDtoV1 cancelOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_PENDING);
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(order.getCreatedAt(), now);
        if (duration.toMinutes() >= 5) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
        }

        order.cancelOrder();
        return ResGetOrderDtoV1.from(order, "주문이 성공적으로 취소되었습니다.");
    }

    /**
     * 주문 요청사항 수정
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderRequest(UUID orderId, String newRequest) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        try {
            order.updateRequest(newRequest); 
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.ORDER_REQUEST_UPDATE_FAILED);
        }
        
        return ResGetOrderDtoV1.from(order, "요청사항이 성공적으로 수정되었습니다.");
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderStatus(UUID orderId, OrderStatus nextStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        
        if (order.getStatus() == OrderStatus.CANCELLED || 
            order.getStatus() == OrderStatus.DELIVERED || 
            order.getStatus() == OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_FINALIZED);
        }

        try {
            order.updateStatus(nextStatus);
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.ORDER_STATUS_UPDATE_FAILED);
        }
        
        return ResGetOrderDtoV1.from(order, "주문 상태가 변경되었습니다.");
    }

    /**
     * 주문 삭제
     */
    @Transactional
    public void deleteOrder(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_DELETE_DELIVERED);
        }

        order.softDelete(MOCK_USER_ID); 
    }
}
