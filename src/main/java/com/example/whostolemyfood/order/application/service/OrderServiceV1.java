package com.example.whostolemyfood.order.application.service;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import com.example.whostolemyfood.address.domain.repository.AddressRepository;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
import com.example.whostolemyfood.store.domain.entity.StoreEntity;
import com.example.whostolemyfood.store.domain.entity.StoreStatus;
import com.example.whostolemyfood.store.domain.repository.StoreRepository;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final AddressRepository addressRepository;

    /**
     * 주문 생성 (가게 운영 정책 검증 포함)
     */
    @Transactional
    public ResCreateOrderDtoV1 createOrder(ReqCreateOrderDtoV1 request, UUID userId) {
        log.info("[Order] Creating order. User: {}, Store: {}", userId, request.getStoreId());

        // 1. 가게 존재 여부 및 운영 정책 확인
        StoreEntity store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        // [보안] 숨김 처리된 가게면 주문 차단
        if (Boolean.TRUE.equals(store.getIsHidden())) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }

        // [상태] 영업 중인 가게인지 확인
        if (store.getStatus() != StoreStatus.OPEN) {
            throw new CustomException(ErrorCode.STORE_CLOSED);
        }

        // [영업시간] 현재 주문 가능한 시간인지 확인
        LocalTime now = LocalTime.now();
        if (now.isBefore(store.getOpenTime()) || now.isAfter(store.getCloseTime())) {
            log.warn("[Order] Store is not in operating hours. Store: {}, Current: {}", store.getId(), now);
            throw new CustomException(ErrorCode.STORE_CLOSED);
        }

        // 2. 배송지 존재 여부 및 소유권 확인
        AddressEntity address = addressRepository.findByIdAndIsDeletedFalse(request.getAddressId())
                .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ADDRESS_NOT_OWNER);
        }

        // 3. 메뉴 가격 검증 및 음식 총액 계산
        int calculatedItemTotalPrice = 0;
        for (ReqCreateOrderDtoV1.OrderItemRequest itemRequest : request.getOrderItems()) {
            MenuEntity menu = menuRepository.findById(itemRequest.getMenuId())
                    .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
            
            if (!menu.getPrice().equals(itemRequest.getPriceAtOrder())) {
                throw new CustomException(ErrorCode.PRICE_MISMATCH);
            }
            calculatedItemTotalPrice += menu.getPrice() * itemRequest.getQuantity();
        }

        // [최소주문금액] 검증
        if (store.getMinOrderPrice() != null && calculatedItemTotalPrice < store.getMinOrderPrice()) {
            throw new CustomException(ErrorCode.ORDER_MIN_PRICE_NOT_MET);
        }

        int deliveryFee = 3000; 
        int finalTotalPrice = calculatedItemTotalPrice + deliveryFee;

        // 4. 엔티티 생성
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .storeId(request.getStoreId())
                .addressId(request.getAddressId())
                .request(request.getRequest())
                .totalPrice(finalTotalPrice)
                .deliveryFee(deliveryFee)
                .status(OrderStatus.PENDING)
                .build();
        
        order.markCreatedBy(userId);

        List<OrderItemEntity> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> OrderItemEntity.builder()
                        .order(order)
                        .menuId(itemRequest.getMenuId())
                        .quantity(itemRequest.getQuantity())
                        .priceAtOrder(itemRequest.getPriceAtOrder())
                        .createdBy(userId)
                        .build())
                .toList();

        order.getOrderItems().addAll(orderItems);

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("[Order] Successfully created. OrderId: {}", savedOrder.getOrderId());
        return ResCreateOrderDtoV1.from(savedOrder, "주문이 성공적으로 생성되었습니다.");
    }

    /**
     * 주문 상세 조회
     */
    public ResGetOrderDtoV1 getOrder(UUID orderId, UUID userId, UserRole role) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (role == UserRole.CUSTOMER && !order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ORDER_NOT_OWNER);
        }

        return ResGetOrderDtoV1.from(order);
    }

    /**
     * 주문 목록 조회
     */
    public Page<ResGetOrderListDtoV1> getOrders(UUID storeId, Boolean isHidden, Pageable pageable, UUID userId, UserRole role) {
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
    public ResGetOrderDtoV1 cancelOrder(UUID orderId, UUID userId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ORDER_NOT_OWNER);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_PENDING);
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(order.getCreatedAt(), now);
        if (duration.toMinutes() >= 5) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_TIME_EXCEEDED);
        }

        order.cancelOrder();
        order.markUpdatedBy(userId);
        return ResGetOrderDtoV1.from(order, "주문이 성공적으로 취소되었습니다.");
    }

    /**
     * 주문 요청사항 수정
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderRequest(UUID orderId, String newRequest, UUID userId) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ORDER_NOT_OWNER);
        }

        try {
            order.updateRequest(newRequest); 
            order.markUpdatedBy(userId);
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.ORDER_REQUEST_UPDATE_FAILED);
        }
        
        return ResGetOrderDtoV1.from(order, "요청사항이 성공적으로 수정되었습니다.");
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public ResGetOrderDtoV1 updateOrderStatus(UUID orderId, OrderStatus nextStatus, UUID userId, UserRole role) {
        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        
        if (role == UserRole.CUSTOMER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            order.updateStatus(nextStatus);
            order.markUpdatedBy(userId);
        } catch (IllegalStateException e) {
            throw new CustomException(ErrorCode.ORDER_STATUS_UPDATE_FAILED);
        }
        
        return ResGetOrderDtoV1.from(order, "주문 상태가 변경되었습니다.");
    }

    /**
     * 주문 삭제
     */
    @Transactional
    public void deleteOrder(UUID orderId, UUID userId, UserRole role) {
        if (role != UserRole.MANAGER && role != UserRole.MASTER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        OrderEntity order = orderRepository.findById(orderId)
                .filter(o -> !o.getIsDeleted())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_CANNOT_DELETE_DELIVERED);
        }

        order.softDelete(userId); 
    }
}
