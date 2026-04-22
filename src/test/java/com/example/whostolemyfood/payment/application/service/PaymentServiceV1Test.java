package com.example.whostolemyfood.payment.application.service;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.payment.domain.PaymentStatus;
import com.example.whostolemyfood.payment.domain.PaymentType;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqMakePay;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqModifyPay;
import com.example.whostolemyfood.payment.presentation.dto.response.PageRes;
import com.example.whostolemyfood.payment.presentation.dto.response.ResGetPayById;
import com.example.whostolemyfood.payment.presentation.dto.response.ResMakePay;
import com.example.whostolemyfood.payment.presentation.dto.response.ResModifyPay;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceV1Test {

    @Autowired
    PaymentServiceV1 paymentServiceV1;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void getPayments_결제_페이징_조회() throws JsonProcessingException {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        //when
        PageRes payments = paymentServiceV1.getPayments(pageable);
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(payments);
        //then
        System.out.println(json);

    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void getPaymentById_결제_상세_조회() throws JsonProcessingException {
        //given
        ResGetPayById paymentById = paymentServiceV1.getPaymentById(UUID.fromString("400e8ec0-3b73-488f-87f8-2df2100cbc2c"));
        //when
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(paymentById);
        //then
        System.out.println(json);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void postPayment_결제_생성() throws JsonProcessingException {
        ReqMakePay reqMakePay = new ReqMakePay("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e", 1000L,"paymentKey", "테스트주문1", PaymentType.CARD);

        ResMakePay resMakePay = paymentServiceV1.postPayment(reqMakePay);

        //when
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(resMakePay);
        //then
        System.out.println(json);
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void updatePayment_결제_수정() throws JsonProcessingException {
        ReqModifyPay pay = new ReqModifyPay("af5650fc-24af-443f-b913-58471b088ee6", PaymentStatus.CANCELED);
        ResModifyPay resModifyPay = paymentServiceV1.updatePayment(pay);
        //when
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(resModifyPay);
        //then
        System.out.println(json);
    }
}