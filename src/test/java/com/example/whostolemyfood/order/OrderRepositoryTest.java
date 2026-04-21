package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.JpaAuditingConfig;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 저장 및 조회 시 UUID PK와 상태값이 정확해야 함")
    void saveAndFindOrderTest() {
        OrderEntity order = createOrder(UUID.randomUUID());
        OrderEntity savedOrder = orderRepository.save(order);

        OrderEntity foundOrder = orderRepository.findById(savedOrder.getOrderId()).orElseThrow();
        assertThat(foundOrder.getOrderId()).isNotNull();
        assertThat(foundOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("특정 가게의 주문만 필터링하여 조회할 수 있어야 함")
    void findAllByStoreIdTest() {
        UUID storeA = UUID.randomUUID();
        UUID storeB = UUID.randomUUID();
        
        orderRepository.save(createOrder(storeA));
        orderRepository.save(createOrder(storeA));
        orderRepository.save(createOrder(storeB));

        Page<OrderEntity> orders = orderRepository.findAllByStoreId(storeA, PageRequest.of(0, 10));
        assertThat(orders.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("주문 삭제 시 물리 삭제가 아닌 Soft Delete(isDeleted=true)가 되어야 함")
    void softDeleteTest() {
        OrderEntity order = orderRepository.save(createOrder(UUID.randomUUID()));
        
        order.markAsDeleted(UUID.randomUUID());
        orderRepository.saveAndFlush(order);

        OrderEntity foundOrder = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertThat(foundOrder.getIsDeleted()).isTrue();
        assertThat(foundOrder.getDeletedAt()).isNotNull();
    }

    private OrderEntity createOrder(UUID storeId) {
        return OrderEntity.builder()
                .userId(UUID.randomUUID())
                .storeId(storeId)
                .addressId(UUID.randomUUID())
                .totalPrice(10000)
                .deliveryFee(3000)
                .status(OrderStatus.PENDING)
                .build();
    }
}
