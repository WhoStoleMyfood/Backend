package com.example.whostolemyfood.order.domain.repository;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * [Repository] 주문(Order) 도메인 전용 리포지토리 인터페이스
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * 특정 가게의 주문 목록을 페이징하여 조회
     */
    Page<OrderEntity> findAllByStoreId(UUID storeId, Pageable pageable);

    /**
     * 가게 ID 및 숨김 여부에 따른 필터링 조회
     */
    Page<OrderEntity> findAllByStoreIdAndIsHidden(UUID storeId, Boolean isHidden, Pageable pageable);

    /**
     * 전체 주문 중 숨김 여부에 따른 필터링 조회
     */
    Page<OrderEntity> findAllByIsHidden(Boolean isHidden, Pageable pageable);

    /**
     * 결제를 위한 주문 존재 여부 조회
     */
    Optional<OrderEntity> findByOrderIdAndUserId(UUID orderId, UUID userId);
}
