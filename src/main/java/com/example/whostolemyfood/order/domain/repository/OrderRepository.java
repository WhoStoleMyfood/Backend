package com.example.whostolemyfood.order.domain.repository;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * 기본 페이징 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByIsDeletedFalse(@Param("pageable") Pageable pageable);

    /**
     * 특정 가게의 활성 주문 목록 조회
     */
    Page<OrderEntity> findAllByStoreIdAndIsDeletedFalse(@Param("storeId") UUID storeId, @Param("pageable") Pageable pageable);

    /**
     * 가게 ID 및 숨김 여부에 따른 필터링 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByStoreIdAndIsHiddenAndIsDeletedFalse(@Param("storeId") UUID storeId, @Param("isHidden") Boolean isHidden, @Param("pageable") Pageable pageable);

    /**
     * 전체 주문 중 숨김 여부에 따른 필터링 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByIsHidden(@Param("isHidden") Boolean isHidden, @Param("pageable") Pageable pageable);

    /**
     * 결제를 위한 주문 존재 여부 조회
     */
    Optional<OrderEntity> findByOrderIdAndUserId(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    Page<OrderEntity> findAllByIsHiddenAndIsDeletedFalse(@Param("isHidden") Boolean isHidden, @Param("pageable") Pageable pageable);

    /**
     * Soft Delete가 false인지 검사
     */
    Optional<OrderEntity> findByOrderIdAndIsDeletedFalse(@Param("orderId") UUID orderId);

    // [RBAC] CUSTOMER: 본인의 주문만 조회
    Page<OrderEntity> findAllByUserIdAndIsDeletedFalse(@Param("userId") UUID userId, @Param("pageable") Pageable pageable);
}
