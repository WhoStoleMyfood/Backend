package com.example.whostolemyfood.payment.application.service;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.order.domain.entity.OrderEntity;
import com.example.whostolemyfood.order.domain.repository.OrderRepository;
import com.example.whostolemyfood.payment.domain.PaymentEntity;
import com.example.whostolemyfood.payment.domain.PaymentStatus;
import com.example.whostolemyfood.payment.domain.PaymentType;
import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
class PaymentServiceV1Test {

    @Autowired
    PaymentServiceV1 paymentServiceV1;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void getPayments_결제_페이징_조회() throws JsonProcessingException {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        //when
        PageRes payments = paymentServiceV1.getPayments(pageable);
        // then
        assertThat(payments.getContent()).isNotEmpty();

    }

    @Test
    void getPayments_유저가_존재하지_않는_경우() throws JsonProcessingException {
        //given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        //when then
        CustomException customException = assertThrows(CustomException.class, () -> paymentServiceV1.getPayments(pageable));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);

    }

    @Test
    @Transactional
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void getPaymentById_결제_상세_조회() throws JsonProcessingException {

        ReqMakePay reqMakePay = new ReqMakePay("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e", 1000L,"paymentKey3", "테스트주문1", PaymentType.CARD);
        ResMakePay resMakePay = paymentServiceV1.postPayment(reqMakePay);

        //given
        ResGetPayById paymentById = paymentServiceV1.getPaymentById(resMakePay.getPaymentId());
        //when
        assertThat(paymentById.getPaymentId()).isEqualTo(resMakePay.getPaymentId());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void getPaymentById_존재하지_않는_결제_조회() throws JsonProcessingException {

        //given
        CustomException customException = assertThrows(CustomException.class, () -> paymentServiceV1.getPaymentById(UUID.fromString("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e")));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);

    }

    @Test
    @Transactional
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

    @DisplayName("payment 유니크 키 중복")
    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void postPayment_결제_생성_실패() throws JsonProcessingException {
        ReqMakePay reqMakePay = new ReqMakePay("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e", 1000L,"paymentKey", "테스트주문1", PaymentType.CARD);

        assertThrows(DataIntegrityViolationException.class ,()-> paymentServiceV1.postPayment(reqMakePay));

    }

    @Test
    @DisplayName("payment상태가 DONE이 아닌 경우 실패")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void updatePayment_결제_수정_실패() throws JsonProcessingException {
        ReqModifyPay pay = new ReqModifyPay("af5650fc-24af-443f-b913-58471b088ee6", PaymentStatus.CANCELED);
        CustomException customException = assertThrows(CustomException.class, () -> paymentServiceV1.updatePayment(pay));

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.FAIL_TO_MODIFY_STATUS);

    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "CUSTOMER")
    void updatePayment_결제_수정_성공() throws JsonProcessingException {
        //given
        UUID orderId = UUID.fromString("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e");

        OrderEntity referenceById = orderRepository.getReferenceById(orderId);

        PaymentEntity paymentEntity = new PaymentEntity(null, referenceById, 1000L, PaymentType.CARD, PaymentStatus.DONE, "payemntKey11", LocalDateTime.now());
        paymentEntity.setCreatedInfo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));

        paymentRepository.save(paymentEntity);

        UUID id = paymentEntity.getId();
        ReqModifyPay pay = new ReqModifyPay(id.toString(), PaymentStatus.CANCELED);

        //when
        ResModifyPay resModifyPay = paymentServiceV1.updatePayment(pay);
        //then
        assertThat(resModifyPay.getPaymentId()).isEqualTo(id);

    }

    @Test
    void createToken () {
        UUID userID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String token = jwtUtil.createToken(userID, UserRole.CUSTOMER);
        log.info("{}", token);
    }
}