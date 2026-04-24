package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.presentation.controller.OrderControllerV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderStatusDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderControllerV1.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderServiceV1 orderService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private AuthUser authUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        authUser = new AuthUser(userId, "tester@example.com", UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("[성공] 주문 생성 API - 200 OK 및 데이터 정밀 검증")
    void createOrderApiTest() throws Exception {
        UUID expectedOrderId = UUID.randomUUID();
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder().menuId(UUID.randomUUID()).quantity(1).priceAtOrder(10000).build()))
                .build();
        ResCreateOrderDtoV1 response = ResCreateOrderDtoV1.builder()
                .orderId(expectedOrderId)
                .message("주문이 성공적으로 생성되었습니다.")
                .build();
        
        given(orderService.createOrder(any(), eq(userId))).willReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(expectedOrderId.toString()))
                .andExpect(jsonPath("$.message").value("주문이 성공적으로 생성되었습니다."));
    }

    @Test
    @DisplayName("[성공] 주문 상세 조회 API - 데이터 정합성 검증")
    void getOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();
        
        given(orderService.getOrder(eq(orderId), eq(userId), eq(UserRole.CUSTOMER))).willReturn(response);

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("[성공] 주문 목록 조회 API - PageResponse 반환 확인")
    void getOrdersApiTest() throws Exception {
        given(orderService.getOrders(any(), any(), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/orders")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("[성공] 주문 취소 API - 결과 상태값 검증")
    void cancelOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder()
                .orderId(orderId)
                .status(OrderStatus.CANCELLED)
                .build();
        
        given(orderService.cancelOrder(eq(orderId), eq(userId))).willReturn(response);

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("[성공] 주문 삭제 API - 204 No Content")
    void deleteOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        AuthUser admin = new AuthUser(UUID.randomUUID(), "admin@example.com", UserRole.MANAGER);

        mockMvc.perform(delete("/api/v1/orders/" + orderId)
                        .with(user(admin)))
                .andExpect(status().isNoContent());
    }
}
