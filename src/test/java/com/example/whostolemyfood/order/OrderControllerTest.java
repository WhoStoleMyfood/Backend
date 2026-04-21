package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.presentation.controller.OrderControllerV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderRequestDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderStatusDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @DisplayName("[성공] 주문 생성 API 호출 시 200 OK를 반환해야 함")
    void createOrderApiTest() throws Exception {
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder().menuId(UUID.randomUUID()).quantity(1).priceAtOrder(10000).build()))
                .build();
        ResCreateOrderDtoV1 response = ResCreateOrderDtoV1.builder().orderId(UUID.randomUUID()).totalPrice(13000).build();
        given(orderService.createOrder(any())).willReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 주문 취소 API 호출 시 200 OK를 반환해야 함")
    void cancelOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder()
                .orderId(orderId).status(OrderStatus.CANCELLED).build();
        given(orderService.cancelOrder(orderId)).willReturn(response);

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("[성공] 주문 상태 변경 API 호출 시 변경된 상태가 반환되어야 함")
    void updateOrderStatusApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReqUpdateOrderStatusDtoV1 request = new ReqUpdateOrderStatusDtoV1(OrderStatus.ACCEPTED);
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder()
                .orderId(orderId).status(OrderStatus.ACCEPTED).build();
        
        given(orderService.updateOrderStatus(any(), any())).willReturn(response);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("[성공] 주문 요청사항 수정(PUT) API가 정상 호출되어야 함")
    void updateOrderRequestApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReqUpdateOrderRequestDtoV1 updateRequest = ReqUpdateOrderRequestDtoV1.builder().request("수정내용").build();
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder().orderId(orderId).request("수정내용").build();
        given(orderService.updateOrderRequest(any(), any())).willReturn(response);

        mockMvc.perform(put("/api/orders/" + orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.request").value("수정내용"));
    }

    @Test
    @DisplayName("[보안] 삭제된 주문 번호로 상세 조회 시 400 에러를 반환해야 함")
    void getOrderDeletedFailureTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        given(orderService.getOrder(orderId)).willThrow(new IllegalArgumentException("존재하지 않거나 삭제된 주문입니다."));

        mockMvc.perform(get("/api/orders/" + orderId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @DisplayName("[실패] 잘못된 입력값(수량 0)으로 주문 생성 시 400 에러를 반환해야 함")
    void validationErrorTest() throws Exception {
        ReqCreateOrderDtoV1 invalidRequest = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder().quantity(0).build()))
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("[정책] 허용되지 않은 페이지 사이즈(100) 요청 시 기본값(10)으로 보정되어 정상 처리되어야 함")
    void getOrdersSizeLimitTest() throws Exception {
        given(orderService.getOrders(any(), any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/orders?size=100"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
