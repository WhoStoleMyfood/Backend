package com.example.whostolemyfood.payment.domain;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.payment.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Table(name = "p_payments")
@Entity
@Getter
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    @ManyToOne(FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType payType;

    @Column(nullable = false)
    private PaymentStatus payStatus;

    @Column(unique = true, nullable = false)
    private String paymentKey;

    private LocalDateTime approvedAt;

}
