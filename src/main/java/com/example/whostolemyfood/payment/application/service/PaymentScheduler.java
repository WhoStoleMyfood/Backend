//package com.example.whostolemyfood.payment.application.service;
//
//import com.example.whostolemyfood.payment.domain.PaymentEntity;
//import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class PaymentScheduler {
//
//    private final PaymentRepository paymentRepository;
//    private final PaymentServiceExtend paymentServiceExtend;
//
//    @Scheduled(fixedRate = 60000)
//    public void checkReady () {
//
//        List<PaymentEntity> allByCreatedAtLessThan = paymentRepository.findAllByCreatedAtLessThan(LocalDateTime.now().minusMinutes(10));
//        for(PaymentEntity p:allByCreatedAtLessThan) {
//            try{
//                String paymentKey = p.getPaymentKey();
//                String s = paymentServiceExtend.checkPayment(paymentKey);
//                if(s.equals("DONE")){
//                    paymentServiceExtend.updatePaymentStatus(paymentKey, "DONE");
//                }else{
//                    paymentServiceExtend.updatePaymentStatus(paymentKey, "FAIL");
//                }
//            } catch (Exception e) {
//                log.error("결제 복구 실패 : {}", e.getMessage());
//            }
//        }
//    }
//
//}
