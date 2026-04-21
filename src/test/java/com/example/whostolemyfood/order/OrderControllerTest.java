package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.presentation.controller.OrderControllerV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderControllerV1.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderServiceV1 orderService;

    @Test
    @DisplayName("잘못된 입력값(수량 0)으로 주문 생성 시 400 에러를 반환해야 함")
    void validationErrorTest() throws Exception {
        // Given: 수량이 0인 잘못된 요청 데이터를 만듭니다.
        ReqCreateOrderDtoV1 invalidRequest = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID())
                .addressId(UUID.randomUUID())
                .orderItems(List.of(
                        ReqCreateOrderDtoV1.OrderItemRequest.builder()
                                .menuId(UUID.randomUUID())
                                .quantity(0) // 유효성 검사 위반
                                .priceAtOrder(10000)
                                .build()
                ))
                .build();

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print()) // 콘솔에 상세 로그를 출력
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("VALIDATION_ERROR")) // code -> message 로 변경
                .andExpect(jsonPath("$.errors[0].field").value("orderItems[0].quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("수량은 최소 1개 이상이어야 합니다.")); // reason -> message 로 변경

        verifyNoInteractions(orderService);
    }
}
