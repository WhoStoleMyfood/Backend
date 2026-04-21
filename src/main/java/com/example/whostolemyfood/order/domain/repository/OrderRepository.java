package com.example.whostolemyfood.order.domain.repository;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

/**
 * [Repository] 주문(Order) 도메인 전용 리포지토리 인터페이스
 * - 실무 표준에 따라 모든 조회 시 논리 삭제(isDeleted = false)된 데이터만 필터링합니다.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * 기본 페이징 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * 특정 가게의 활성 주문 목록 조회
     */
    Page<OrderEntity> findAllByStoreIdAndIsDeletedFalse(UUID storeId, Pageable pageable);

    /**
     * 가게 ID 및 숨김 여부에 따른 필터링 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByStoreIdAndIsHiddenAndIsDeletedFalse(UUID storeId, Boolean isHidden, Pageable pageable);

    /**
     * 전체 주문 중 숨김 여부에 따른 필터링 조회 (삭제된 데이터 제외)
     */
    Page<OrderEntity> findAllByIsHiddenAndIsDeletedFalse(Boolean isHidden, Pageable pageable);
}
