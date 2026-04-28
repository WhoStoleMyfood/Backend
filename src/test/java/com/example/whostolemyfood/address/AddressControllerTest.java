package com.example.whostolemyfood.address;

import com.example.whostolemyfood.address.application.service.AddressServiceV1;
import com.example.whostolemyfood.address.presentation.controller.AddressControllerV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.request.ReqUpdateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResCreateAddressDtoV1;
import com.example.whostolemyfood.address.presentation.dto.response.ResGetAddressDtoV1;
import com.example.whostolemyfood.global.config.security.SecurityConfig;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
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

@WebMvcTest(AddressControllerV1.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@AutoConfigureMockMvc
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressServiceV1 addressService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private AuthUser customerUser;
    private AuthUser ownerUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        customerUser = new AuthUser(userId, "customer@example.com", UserRole.CUSTOMER);
        ownerUser = new AuthUser(UUID.randomUUID(), "owner@example.com", UserRole.OWNER);
    }

    @Test
    @DisplayName("[성공] 배송지 생성 API - 200 OK")
    void createAddress_Authorized() throws Exception {
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder()
                .alias("우리집").address("서울특별시 종로구").detail("101호").zipCode("12345").isDefault(true)
                .build();
        ResCreateAddressDtoV1 response = ResCreateAddressDtoV1.builder().addressId(UUID.randomUUID()).build();
        
        given(addressService.createAddress(any(), eq(userId), eq(UserRole.CUSTOMER))).willReturn(response);

        mockMvc.perform(post("/api/v1/addresses")
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print()) // 로깅 추가
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 본인 배송지 목록 조회 API - 200 OK")
    void getMyAddresses_Authorized() throws Exception {
        given(addressService.getMyAddresses(eq(userId), eq(UserRole.CUSTOMER), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/addresses")
                        .with(user(customerUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("[인가 실패] 타인의 배송지 수정 시도 - 403 Forbidden")
    void updateAddress_NotOwner() throws Exception {
        UUID addressId = UUID.randomUUID();
        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder().address("해킹시도").build();
        
        given(addressService.updateAddress(eq(addressId), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willThrow(new CustomException(ErrorCode.ADDRESS_NOT_OWNER));

        mockMvc.perform(put("/api/v1/addresses/" + addressId)
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AD002"));
    }

    @Test
    @DisplayName("[비즈니스 실패] 존재하지 않는 배송지 수정 시도 - 404 Not Found")
    void updateAddress_NotFound() throws Exception {
        UUID addressId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder().address("유령").build();

        given(addressService.updateAddress(eq(addressId), any(), eq(userId), eq(UserRole.CUSTOMER)))
                .willThrow(new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        mockMvc.perform(put("/api/v1/addresses/" + addressId)
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AD001"));
    }

    @Test
    @DisplayName("[유효성 실패] 필수값(address) 누락 시 - 400 Bad Request")
    void createAddress_ValidationError() throws Exception {
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder()
                .alias("주소없음")
                .address("") // @NotBlank 위반
                .build();

        mockMvc.perform(post("/api/v1/addresses")
                        .with(user(customerUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[성공] 배송지 삭제 API - 204 No Content")
    void deleteAddress_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/addresses/" + UUID.randomUUID())
                        .with(user(customerUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[요구사항] 페이지 사이즈 제한 - 100건 요청 시 기본 10건으로 제한되는지 확인")
    void getMyAddresses_PagingLimit_Validation() throws Exception {
        // Given
        given(addressService.getMyAddresses(eq(userId), eq(UserRole.CUSTOMER), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        // When: size=100 이라는 허용되지 않은 사이즈로 요청
        mockMvc.perform(get("/api/v1/addresses?size=100")
                        .with(user(customerUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // Then: 서비스에는 교정된 사이즈인 10이 전달되어야 함 (argThat 검증)
        org.mockito.Mockito.verify(addressService).getMyAddresses(
                eq(userId), 
                eq(UserRole.CUSTOMER), 
                any(), 
                org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 10)
        );
    }
    }

