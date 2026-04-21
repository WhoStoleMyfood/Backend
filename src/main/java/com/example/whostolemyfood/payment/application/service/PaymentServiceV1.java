package com.example.whostolemyfood.payment.application.service;

import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.payment.base.AuditorAwareImpl;
import com.example.whostolemyfood.payment.domain.PaymentEntity;
import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqModifyPay;
import com.example.whostolemyfood.payment.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceV1 {

    private final PaymentRepository paymentRepository;
    private final AuditorAwareImpl auditor;
    private final OrderRepository orderRepository;

    public PageRes getPayments(Pageable pageable) {

        UUID currentAuditor = auditor.getCurrentAuditor().orElseThrow(()-> new RuntimeException("존재하지 않는 유저입니다."));
        if(pageable.getPageSize()>50){
            pageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
        }
        Page<PaymentEntity> allByCreatedBy = paymentRepository.findAllByCreatedBy(currentAuditor, pageable);
        Page<ResPayList> map = allByCreatedBy.map(ResPayList::new);
        return new PageRes(map);

    }

    public ResGetPayById getPaymentById(UUID id) {

        UUID currentAuditor = auditor.getCurrentAuditor().orElseThrow(()-> new RuntimeException("존재하지 않는 유저입니다."));
        PaymentEntity allByCreatedByAndId = paymentRepository.findAllByCreatedByAndId(currentAuditor, id).orElseThrow(()-> new RuntimeException("존재하지 않는 결제 내역입니다."));
        return new ResGetPayById(allByCreatedByAndId);

    }

    @Transactional
    public ResMakePay postPayment(ReqMakePay reqMakePay) {

        UUID currentAuditor = auditor.getCurrentAuditor().orElseThrow(()-> new RuntimeException("존재하지 않는 유저입니다."));
        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(UUID.fromString(reqMakePay.getOrderId()), currentAuditor).orElseThrow(() -> new RuntimeException("존재하지 않는 주문입니다."));
        PaymentEntity paymentEntity = new PaymentEntity(reqMakePay, orderEntity);
        paymentRepository.save(paymentEntity);
        return new ResMakePay(paymentEntity.getId(), paymentEntity.getPaymentKey());

    }

    @Transactional
    public ResModifyPay updatePayment(ReqModifyPay reqModifyPay) {

        UUID currentAuditor = auditor.getCurrentAuditor().orElseThrow(()-> new RuntimeException("존재하지 않는 유저입니다."));
        PaymentEntity allByCreatedByAndId = paymentRepository.findAllByCreatedByAndId(currentAuditor, UUID.fromString(reqModifyPay.getPaymentId())).orElseThrow(()-> new RuntimeException("존재하지 않는 결제 내역입니다."));
        allByCreatedByAndId.payCancel();
        return new ResModifyPay(allByCreatedByAndId.getId(), LocalDateTime.now());

    }
}
