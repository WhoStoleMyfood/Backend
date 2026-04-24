package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.presentation.controller.OrderControllerV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderRequestDtoV1;
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

    private AuthUser customerUser;
    private AuthUser masterUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        customerUser = new AuthUser(userId, "customer@example.com", UserRole.CUSTOMER);
        masterUser = new AuthUser(UUID.randomUUID(), "master@example.com", UserRole.MASTER);
    }

    @Test
    @DisplayName("[성공] 주문 생성 - 200 OK")
    void createOrderApiTest() throws Exception {
        ReqCreateOrderDtoV1 request = ReqCreateOrderDtoV1.builder()
                .storeId(UUID.randomUUID()).addressId(UUID.randomUUID())
                .orderItems(List.of(ReqCreateOrderDtoV1.OrderItemRequest.builder().menuId(UUID.randomUUID()).quantity(1).priceAtOrder(10000).build()))
                .build();
        ResCreateOrderDtoV1 response = ResCreateOrderDtoV1.builder().orderId(UUID.randomUUID()).message("성공").build();
        
        given(orderService.createOrder(any(), eq(userId), eq(UserRole.CUSTOMER))).willReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 주문 목록 조회 - 200 OK")
    void getOrdersApiTest() throws Exception {
        given(orderService.getOrders(any(), any(), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/orders")
                        .with(user(customerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("[성공] 주문 상세 조회 - 200 OK")
    void getOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ResGetOrderDtoV1 response = ResGetOrderDtoV1.builder().orderId(orderId).build();
        given(orderService.getOrder(eq(orderId), eq(userId), eq(UserRole.CUSTOMER))).willReturn(response);

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .with(user(customerUser)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 요청사항 수정 - 200 OK")
    void updateOrderRequestApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReqUpdateOrderRequestDtoV1 request = new ReqUpdateOrderRequestDtoV1("수정된 요청");
        given(orderService.updateOrderRequest(eq(orderId), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(ResGetOrderDtoV1.builder().orderId(orderId).build());

        mockMvc.perform(put("/api/v1/orders/" + orderId)
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 상태 변경 - 200 OK")
    void updateOrderStatusApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReqUpdateOrderStatusDtoV1 request = new ReqUpdateOrderStatusDtoV1(OrderStatus.ACCEPTED);
        given(orderService.updateOrderStatus(eq(orderId), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(ResGetOrderDtoV1.builder().orderId(orderId).build());

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 주문 취소 - 200 OK")
    void cancelOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        given(orderService.cancelOrder(eq(orderId), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(ResGetOrderDtoV1.builder().orderId(orderId).build());

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel")
                        .with(user(customerUser)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 주문 삭제 - 204 No Content")
    void deleteOrderApiTest() throws Exception {
        UUID orderId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/orders/" + orderId)
                        .with(user(masterUser)))
                .andExpect(status().isNoContent());
    }
}
