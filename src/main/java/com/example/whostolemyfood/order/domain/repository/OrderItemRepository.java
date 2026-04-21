package com.example.whostolemyfood.order.domain.repository;

import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
}
