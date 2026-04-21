package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.JpaAuditingConfig;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // 실제 데이터베이스를 사용하므로 테스트 간의 독립성을 위해 데이터를 초기화합니다.
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("기본 페이징 조회 시 삭제되지 않은 주문만 조회되어야 함")
    void findAllByIsDeletedFalseTest() {
        // Given
        OrderEntity activeOrder = orderRepository.save(createOrder(UUID.randomUUID()));
        OrderEntity deletedOrder = orderRepository.save(createOrder(UUID.randomUUID()));
        deletedOrder.markAsDeleted(UUID.randomUUID());
        orderRepository.saveAndFlush(deletedOrder);

        // When
        Page<OrderEntity> result = orderRepository.findAllByIsDeletedFalse(PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getOrderId()).isEqualTo(activeOrder.getOrderId());
    }

    @Test
    @DisplayName("특정 가게의 활성 주문만 필터링하여 조회할 수 있어야 함")
    void findAllByStoreIdAndIsDeletedFalseTest() {
        // Given
        UUID storeA = UUID.randomUUID();
        orderRepository.save(createOrder(storeA));
        orderRepository.save(createOrder(storeA));
        
        OrderEntity deletedOrder = orderRepository.save(createOrder(storeA));
        deletedOrder.markAsDeleted(UUID.randomUUID());
        orderRepository.saveAndFlush(deletedOrder);

        // When
        Page<OrderEntity> orders = orderRepository.findAllByStoreIdAndIsDeletedFalse(storeA, PageRequest.of(0, 10));

        // Then
        assertThat(orders.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("숨김 처리된 주문(isHidden=true)만 필터링하여 조회할 수 있어야 함")
    void findAllByIsHiddenAndIsDeletedFalseTest() {
        // Given
        orderRepository.save(OrderEntity.builder()
                .userId(UUID.randomUUID()).storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .totalPrice(10000).deliveryFee(3000).status(OrderStatus.PENDING).isHidden(true).build());
        
        orderRepository.save(OrderEntity.builder()
                .userId(UUID.randomUUID()).storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .totalPrice(10000).deliveryFee(3000).status(OrderStatus.PENDING).isHidden(false).build());

        // When
        Page<OrderEntity> hiddenOrders = orderRepository.findAllByIsHiddenAndIsDeletedFalse(true, PageRequest.of(0, 10));

        // Then
        assertThat(hiddenOrders.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("특정 가게의 주문 중 숨김 처리된 활성 주문만 필터링할 수 있어야 함")
    void findAllByStoreIdAndIsHiddenAndIsDeletedFalseTest() {
        // Given
        UUID storeId = UUID.randomUUID();
        orderRepository.save(OrderEntity.builder()
                .userId(UUID.randomUUID()).storeId(storeId).addressId(UUID.randomUUID())
                .totalPrice(10000).deliveryFee(3000).status(OrderStatus.PENDING).isHidden(true).build());
        
        orderRepository.save(OrderEntity.builder()
                .userId(UUID.randomUUID()).storeId(storeId).addressId(UUID.randomUUID())
                .totalPrice(10000).deliveryFee(3000).status(OrderStatus.PENDING).isHidden(false).build());

        // When
        Page<OrderEntity> result = orderRepository.findAllByStoreIdAndIsHiddenAndIsDeletedFalse(storeId, true, PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문 삭제 시 Soft Delete(isDeleted=true)가 정상 작동해야 함")
    void softDeleteTest() {
        // Given
        OrderEntity order = orderRepository.save(createOrder(UUID.randomUUID()));
        
        // When
        order.markAsDeleted(UUID.randomUUID());
        orderRepository.saveAndFlush(order);

        // Then
        OrderEntity foundOrder = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertThat(foundOrder.getIsDeleted()).isTrue();
        assertThat(foundOrder.getDeletedAt()).isNotNull();
    }

    private OrderEntity createOrder(UUID storeId) {
        return OrderEntity.builder()
                .userId(UUID.randomUUID())
                .storeId(storeId)
                .addressId(UUID.randomUUID())
                .totalPrice(13000)
                .deliveryFee(3000)
                .status(OrderStatus.PENDING)
                .build();
    }
}
