package com.example.whostolemyfood.payment.application.service;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.payment.domain.PaymentEntity;
import com.example.whostolemyfood.payment.domain.PaymentStatus;
import com.example.whostolemyfood.payment.infrastructure.PaymentRepository;
import com.example.whostolemyfood.payment.presentation.dto.request.ReqConfirmDto;
import com.example.whostolemyfood.payment.presentation.dto.response.TossErrorResDto;
import com.example.whostolemyfood.payment.presentation.dto.response.TossPaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jshell.Snippet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceExtend {

    @Value("${PAYMENT_KEY}")
    private String PAYMENT_KEY;
    private final PaymentRepository paymentRepository;
    private final PaymentServiceV1 paymentServiceV1;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TossPaymentResponse confirm(ReqConfirmDto reqConfirmDto) {

        String auth = Base64.getEncoder().encodeToString((PAYMENT_KEY + ":").getBytes(StandardCharsets.UTF_8));

        TossPaymentResponse response = restClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(reqConfirmDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error("결제 거절: {}", objectMapper.readValue(res.getBody(), TossErrorResDto.class));
                    throw new CustomException(ErrorCode.FAIL_PAY);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("토스 서버 장애");
                    throw new RuntimeException("Toss Server Error");
                })
                .body(TossPaymentResponse.class);

        if("DONE".equals(response.status())){
            if(!response.totalAmount().equals(reqConfirmDto.getAmount())){
                throw new RuntimeException("결제 금액이 다름");
            }

            String detail = "";
            if(response.card()!=null){
                detail = response.card().issuerCode() + "카드";
            } else if(response.easyPay()!=null){
                detail = response.easyPay().provider();
            }
        }

        return response;
    }

    public void payProcess (ReqConfirmDto request) {

        UUID paymentId = paymentServiceV1.makePayment(request);

        try{
            TossPaymentResponse confirm = confirm(request);
            paymentServiceV1.successPay(paymentId);
        }catch (CustomException e) {
            paymentServiceV1.failPay(paymentId);
            throw e;
        }catch (Exception e) {
            log.error("결제 중 오류가 발생하였습니다.");
            throw e;
        }

    }

    public String checkPayment(String paymentKey) {

        String auth = Base64.getEncoder().encodeToString((PAYMENT_KEY + ":").getBytes(StandardCharsets.UTF_8));

        TossPaymentResponse body = restClient.get()
                .uri("https://api.tosspayments.com/v1/payments/{paymentKey}", paymentKey)
                .header("Authorization", "Basic " + auth)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    // 조회의 경우 404가 뜨면 "결제 내역 없음"을 의미함
                    TossErrorResDto error = objectMapper.readValue(res.getBody(), TossErrorResDto.class);
                    log.error("결제 조회 실패 (4xx): {}", error);
                    throw new RuntimeException("결제 내역 없음");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("토스 서버 장애로 인한 조회 불가");
                    throw new RuntimeException("Toss Server Error");
                })
                .body(TossPaymentResponse.class);

        return Objects.requireNonNull(body).status();
    }

    @Transactional
    public void updatePaymentStatus(String paymentKey, String status) {
        PaymentEntity byPaymentKey = paymentRepository.findByPaymentKey(paymentKey);
        byPaymentKey.setPayStatus(PaymentStatus.valueOf(status));
    }

}
