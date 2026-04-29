package com.example.whostolemyfood.payment.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.payment.base.AuditorAwareImpl;
import com.example.whostolemyfood.payment.domain.PaymentEntity;
import com.example.whostolemyfood.payment.domain.PaymentStatus;
import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqConfirmDto;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqModifyPay;
import com.example.whostolemyfood.payment.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

        UUID currentAuditor = getCurrentAuditor();
        if(pageable.getPageSize()!=10&&pageable.getPageSize()!=30&&pageable.getPageSize()!=50){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        Page<PaymentEntity> allByCreatedBy = paymentRepository.findAllByCreatedBy(currentAuditor, pageable);
        Page<ResPayList> map = allByCreatedBy.map(ResPayList::new);
        return new PageRes(map);

    }

    public ResGetPayById getPaymentById(UUID id) {

        UUID currentAuditor = getCurrentAuditor();
        PaymentEntity allByCreatedByAndId = paymentRepository.findAllByCreatedByAndId(currentAuditor, id).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        return new ResGetPayById(allByCreatedByAndId);

    }

    @Transactional
    public ResMakePay postPayment(ReqMakePay reqMakePay) {

        UUID currentAuditor = getCurrentAuditor();
        OrderEntity orderEntity = orderRepository.findByOrderIdAndUserId(UUID.fromString(reqMakePay.getOrderId()), currentAuditor).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if (orderEntity.getTotalPrice().longValue()!=reqMakePay.getAmount()){
            throw new CustomException(ErrorCode.DIFFERENT_PRICE);
        }
        PaymentEntity paymentEntity = new PaymentEntity(reqMakePay, orderEntity, currentAuditor);
        paymentRepository.save(paymentEntity);
        return new ResMakePay(paymentEntity.getId(), paymentEntity.getPaymentKey());

    }

    @Transactional
    public ResModifyPay updatePayment(ReqModifyPay reqModifyPay) {

        UUID currentAuditor = getCurrentAuditor();
        PaymentEntity allByCreatedByAndId = paymentRepository.findAllByCreatedByAndId(currentAuditor, UUID.fromString(reqModifyPay.getPaymentId())).orElseThrow(()-> new RuntimeException("존재하지 않는 결제 내역입니다."));
        allByCreatedByAndId.payCancel(currentAuditor);
        return new ResModifyPay(allByCreatedByAndId.getId(), LocalDateTime.now());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID makePayment (ReqConfirmDto reqConfirmDto) {
        UUID currentAuditor = getCurrentAuditor();
        OrderEntity byOrderIdAndUserId = orderRepository.findByOrderIdAndUserId(UUID.fromString(reqConfirmDto.getOrderId()), currentAuditor).orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        PaymentEntity paymentEntity = new PaymentEntity(reqConfirmDto, byOrderIdAndUserId, currentAuditor);
        PaymentEntity save = paymentRepository.save(paymentEntity);
        return save.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void successPay (UUID paymentId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId).orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        paymentEntity.setPayStatus(PaymentStatus.DONE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPay (UUID paymentId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId).orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        paymentEntity.setPayStatus(PaymentStatus.FAIL);
    }

    private UUID getCurrentAuditor() {
        return auditor.getCurrentAuditor().orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}
