package com.example.whostolemyfood.payment.domain;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.payment.base.BaseTimeEntity;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqConfirmDto;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus payStatus;

    @Column(unique = true, nullable = false)
    private String paymentKey;

    @Setter
    private LocalDateTime approvedAt;

    public PaymentEntity(ReqMakePay reqMakePay, OrderEntity order, UUID userId) {
        this.setCreatedInfo(userId);
        this.amount = reqMakePay.getAmount();
        this.paymentKey = reqMakePay.getPaymentKey();
        this.payType = reqMakePay.getPayType();
        this.approvedAt = LocalDateTime.now();
        this.payStatus = PaymentStatus.DONE;
        this.order = order;
    }

    public PaymentEntity(ReqConfirmDto reqConfirmDto, OrderEntity order, UUID userId) {
        this.setCreatedInfo(userId);
        this.amount = reqConfirmDto.getAmount();
        this.paymentKey = reqConfirmDto.getPaymentKey();
        this.order = order;
        this.payStatus = PaymentStatus.READY;
        this.payType = PaymentType.CARD;
    }

    public void payCancel(UUID userId) {
        if(payStatus == PaymentStatus.DONE) {
            this.setUpdatedInfo(userId);
            this.payStatus = PaymentStatus.CANCELED;
        }else{
            throw new CustomException(ErrorCode.FAIL_TO_MODIFY_STATUS);
        }
    }

}
