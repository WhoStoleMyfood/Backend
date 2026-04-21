package com.example.whostolemyfood.payment.domain;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.payment.base.BaseTimeEntity;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "p_payments")
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType payType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus payStatus;

    @Column(unique = true, nullable = false)
    private String paymentKey;

    private LocalDateTime approvedAt;

    public PaymentEntity(ReqMakePay reqMakePay, OrderEntity order) {
        this.amount = reqMakePay.getAmount();
        this.paymentKey = reqMakePay.getPaymentKey();
        this.payType = reqMakePay.getPayType();
        this.approvedAt = LocalDateTime.now();
        this.payStatus = PaymentStatus.DONE;
        this.order = order;
    }

    public void payCancel() {
        if(payStatus == PaymentStatus.DONE) {
            this.payStatus = PaymentStatus.CANCELED;
        }
    }

}
