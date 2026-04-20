package com.example.whostolemyfood.order.domain.repository;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

	Optional<OrderEntity> findByOrderIdAndIsDeletedFalse(UUID orderId);
}