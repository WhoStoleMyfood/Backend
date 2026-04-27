//package com.example.whostolemyfood.payment.infrastructure;
//
//import com.example.whostolemyfood.order.domain.entity.OrderEntity;
//import com.example.whostolemyfood.order.domain.entity.OrderStatus;
//import com.example.whostolemyfood.order.domain.repository.OrderRepository;
//import com.example.whostolemyfood.payment.domain.PaymentEntity;
//import com.example.whostolemyfood.payment.domain.PaymentStatus;
//import com.example.whostolemyfood.payment.domain.PaymentType;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@EntityScan(basePackages = "com.example.whostolemyfood")
//@EnableJpaRepositories(basePackages ={"com.example.whostolemyfood.payment", "com.example.whostolemyfood.order"})
//@ActiveProfiles("test")
//class PaymentRepositoryTest {
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Test
//    @DisplayName("사용자 ID로 결제 내역 페이징 조회")
//    void findAllByCreatedByTest() {
//        // given
//        UUID userId = UUID.randomUUID();
//        createPayment(userId, "key-1", PaymentStatus.DONE, LocalDateTime.now());
//        createPayment(userId, "key-2", PaymentStatus.DONE, LocalDateTime.now());
//        createPayment(UUID.randomUUID(), "key-3", PaymentStatus.DONE, LocalDateTime.now()); // 다른 유저
//
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // when
//        Page<PaymentEntity> result = paymentRepository.findAllByCreatedBy(userId, pageable);
//
//        // then
//        assertThat(result.getTotalElements()).isEqualTo(2);
//        assertThat(result.getContent()).extracting("paymentKey").containsExactlyInAnyOrder("key-1", "key-2");
//    }
//
//    @Test
//    @DisplayName("사용자 ID와 결제 UUID로 단건 상세 조회")
//    void findAllByCreatedByAndIdTest() {
//        // given
//        UUID userId = UUID.randomUUID();
//        PaymentEntity saved = createPayment(userId, "key-unique", PaymentStatus.DONE, LocalDateTime.now());
//        UUID paymentId = saved.getId();
//
//        // when
//        Optional<PaymentEntity> result = paymentRepository.findAllByCreatedByAndId(userId, paymentId);
//        Optional<PaymentEntity> wrongUserResult = paymentRepository.findAllByCreatedByAndId(UUID.randomUUID(), paymentId);
//
//        // then
//        assertThat(result).isPresent();
//        assertThat(result.get().getPaymentKey()).isEqualTo("key-unique");
//        assertThat(wrongUserResult).isEmpty();
//    }
//
//    @Test
//    @DisplayName("READY 상태이고 10분 이상 지난 좀비 결제건만 조회")
//    void findAllByCreatedAtLessThanTest() {
//        // given
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime tenMinutesAgo = now.minusMinutes(10);
//
//        // 1. 15분 전 READY (조회 대상)
//        PaymentEntity payment = createPayment(UUID.randomUUID(), "zombie-1", PaymentStatus.READY, now.minusMinutes(15));
//        payment.setCreatedAt(now.minusMinutes(15));
//        // 2. 5분 전 READY (조회 대상 아님)
//        PaymentEntity payment1 = createPayment(UUID.randomUUID(), "fresh-ready", PaymentStatus.READY, now.minusMinutes(5));
//        payment1.setCreatedAt(now.minusMinutes(5));
//        // 3. 15분 전 DONE (조회 대상 아님 - 상태가 READY가 아님)
//        PaymentEntity payment2 = createPayment(UUID.randomUUID(), "old-done", PaymentStatus.DONE, now.minusMinutes(15));
//        payment2.setCreatedAt(now.minusMinutes(15));
//        // when
//        List<PaymentEntity> result = paymentRepository.findAllByCreatedAtLessThan(tenMinutesAgo);
//
//        // then
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getPaymentKey()).isEqualTo("zombie-1");
//    }
//
//    @Test
//    @DisplayName("PaymentKey 문자열로 단건 조회")
//    void findByPaymentKeyTest() {
//        // given
//        String targetKey = "toss-payment-key-123";
//        createPayment(UUID.randomUUID(), targetKey, PaymentStatus.DONE, LocalDateTime.now());
//
//        // when
//        PaymentEntity result = paymentRepository.findByPaymentKey(targetKey);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getPaymentKey()).isEqualTo(targetKey);
//    }
//
//    // 테스트용 엔티티 생성 편의 메서드
//    private PaymentEntity createPayment(UUID userId, String key, PaymentStatus status, LocalDateTime createdAt) {
//        OrderEntity order = OrderEntity.builder()
//                .userId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
//                .storeId(UUID.randomUUID())
//                .addressId(UUID.randomUUID())
//                .totalPrice(50000)
//                .status(OrderStatus.PENDING)
//                .deliveryFee(3000)
//                .isHidden(false)
//                .build();
//
//        orderRepository.save(order);
//        PaymentEntity payment = new PaymentEntity(
//                null, // ID 자동 생성
//                order,
//                1000L,
//                PaymentType.CARD,
//                status,
//                key,
//                createdAt
//        );
//        payment.setCreatedInfo(userId); // 작성자 정보 주입
//        return paymentRepository.save(payment);
//    }
//
//}