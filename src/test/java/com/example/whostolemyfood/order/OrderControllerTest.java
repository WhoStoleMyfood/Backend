package com.example.whostolemyfood.order;

import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.domain.entity.OrderStatus;
import com.example.whostolemyfood.order.presentation.controller.OrderControllerV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderRequestDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderStatusDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

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
    private AuthUser ownerUser;
    private AuthUser masterUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        customerUser = new AuthUser(userId, "customer@example.com", UserRole.CUSTOMER);
        ownerUser = new AuthUser(UUID.randomUUID(), "owner@example.com", UserRole.OWNER);
        masterUser = new AuthUser(UUID.randomUUID(), "master@example.com", UserRole.MASTER);
    }

    @Test
    @DisplayName("[인가 성공] 주문 생성 API - CUSTOMER 접근 시 200 OK")
    void createOrder_Authorized() throws Exception {
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[인가 실패] 주문 상태 변경 - CUSTOMER 접근 시 403 Forbidden")
    void updateOrderStatus_Forbidden_Customer() throws Exception {
        UUID orderId = UUID.randomUUID();
        ReqUpdateOrderStatusDtoV1 request = new ReqUpdateOrderStatusDtoV1(OrderStatus.ACCEPTED);

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/status")
                        .with(user(customerUser)) // 고객은 상태 변경 불가
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[인가 실패] 주문 삭제 시도 - CUSTOMER가 호출 시 403 Forbidden")
    void deleteOrder_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/" + UUID.randomUUID())
                        .with(user(customerUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[요구사항] 페이지 사이즈 제한 - 100건 요청 시 기본 10건으로 제한되는지 확인")
    void getOrders_PagingLimit_Validation() throws Exception {
        // Given
        given(orderService.getOrders(any(), any(), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willReturn(new PageImpl<>(List.of()));

        // When: size=100 요청
        mockMvc.perform(get("/api/v1/orders?size=100")
                        .with(user(customerUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // Then: 서비스에는 10이 전달되어야 함 (verify + argThat)
        org.mockito.Mockito.verify(orderService).getOrders(
                any(), any(), 
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 10),
                eq(userId), eq(UserRole.CUSTOMER)
        );
    }

    @Test
    @DisplayName("[성공] 관리자 슈퍼 취소 - MASTER는 200 OK")
    void cancelOrder_ByMaster() throws Exception {
        UUID orderId = UUID.randomUUID();
        given(orderService.cancelOrder(eq(orderId), any(), eq(UserRole.MASTER)))
                .willReturn(ResGetOrderDtoV1.builder().orderId(orderId).build());

        mockMvc.perform(patch("/api/v1/orders/" + orderId + "/cancel")
                        .with(user(masterUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }
}
