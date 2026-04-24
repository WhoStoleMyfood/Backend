package com.example.whostolemyfood.payment.presentation.controller;

import com.example.whostolemyfood.payment.application.service.PaymentServiceExtend;
import com.example.whostolemyfood.payment.application.service.PaymentServiceV1;
import com.example.whostolemyfood.payment.presentation.dto.ResDto;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqConfirmDto;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqModifyPay;
import com.example.whostolemyfood.payment.presentation.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pay")
public class PaymentControllerV1 {

    private final PaymentServiceV1 paymentService;
    private final PaymentServiceExtend paymentServiceExtend;

    @Operation(description = "결제 목록 조회")
    @GetMapping("/list")
    ResponseEntity<?> getPayments(@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageRes<ResPayList> payments = paymentService.getPayments(pageable);
        return ResponseEntity.ok().body(ResDto.success(payments));
    }

    @Operation(description = "결제 단일 상세  조회")
    @GetMapping("/{id}")
    ResponseEntity<?> getPaymentById(@PathVariable("id") UUID id) {
        ResGetPayById paymentById = paymentService.getPaymentById(id);
        return ResponseEntity.ok().body(ResDto.success(paymentById));
    }

    @Operation(description = "결제 요청")
    @PostMapping
    ResponseEntity<?> postPayment(@Valid @RequestBody ReqMakePay reqMakePay) {
        ResMakePay resMakePay = paymentService.postPayment(reqMakePay);
        return ResponseEntity.ok().body(ResDto.success(resMakePay));
    }

    @Operation(description = "결제 상태 변경(취소)")
    @PatchMapping("/modify")
    ResponseEntity<?> updatePayment(@Valid @RequestBody ReqModifyPay reqModifyPay) {
        ResModifyPay resModifyPay = paymentService.updatePayment(reqModifyPay);
        return ResponseEntity.ok().body(ResDto.success(resModifyPay));
    }

    @PostMapping("/confirm")
    ResponseEntity<?> confirm(@RequestBody ReqConfirmDto reqConfirmDto) {
        paymentServiceExtend.payProcess(reqConfirmDto);
        Map<String, String> message = Map.of("message", "success");
        return ResponseEntity.ok().body(message);
    }

}
