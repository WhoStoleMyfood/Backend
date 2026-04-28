package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.JpaAuditingConfig;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.entity.OrderItemEntity;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackageClasses = OrderRepository.class)
@EntityScan(basePackageClasses = {OrderEntity.class, OrderItemEntity.class, UserEntity.class})
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("[레포지토리] Soft Delete 확실성 검증 - flush & clear 후에도 상태 유지 확인")
    void softDelete_StaysDeleted() {
        // Given
        OrderEntity order = orderRepository.save(createOrder(UUID.randomUUID()));
        orderRepository.saveAndFlush(order);

        // When
        order.softDelete(UUID.randomUUID());
        orderRepository.saveAndFlush(order);
        entityManager.clear(); // 1차 캐시 비우기

        // Then
        OrderEntity found = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertThat(found.getIsDeleted()).isTrue();
        assertThat(found.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("[레포지토리] 특정 가게(storeId)의 노출된(isHidden=false) 주문만 페이징 조회")
    void findAllByStoreIdAndIsHiddenAndIsDeletedFalseTest() {
        // Given
        UUID storeId = UUID.randomUUID();
        orderRepository.save(createOrder(storeId, false)); // 대상
        orderRepository.save(createOrder(storeId, true));  // 숨김 처리됨
        orderRepository.save(createOrder(UUID.randomUUID(), false)); // 다른 가게

        // When
        Page<OrderEntity> result = orderRepository.findAllByStoreIdAndIsHiddenAndIsDeletedFalse(
                storeId, false, PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    private OrderEntity createOrder(UUID storeId) {
        return createOrder(storeId, false);
    }

    private OrderEntity createOrder(UUID storeId, boolean isHidden) {
        return OrderEntity.builder()
                .userId(UUID.randomUUID())
                .storeId(storeId)
                .addressId(UUID.randomUUID())
                .totalPrice(13000)
                .deliveryFee(3000)
                .status(OrderStatus.PENDING)
                .isHidden(isHidden)
                .build();
    }
}
