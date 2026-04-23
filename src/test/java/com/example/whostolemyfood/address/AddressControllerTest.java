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

    private AuthUser authUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        authUser = new AuthUser(userId, "test@example.com", UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("[성공] 배송지 생성 API - 200 OK")
    void createAddressApiTest() throws Exception {
        ReqCreateAddressDtoV1 request = ReqCreateAddressDtoV1.builder()
                .alias("집").address("서울특별시 종로구").detail("101호").zipCode("12345").isDefault(true)
                .build();
        ResCreateAddressDtoV1 response = ResCreateAddressDtoV1.builder().addressId(UUID.randomUUID()).build();
        
        given(addressService.createAddress(any(), eq(userId))).willReturn(response);

        mockMvc.perform(post("/api/v1/addresses")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[성공] 본인 배송지 목록 조회 API - 200 OK")
    void getMyAddressesApiTest() throws Exception {
        given(addressService.getMyAddresses(eq(userId), any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/addresses?size=10")
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray()); // PageResponse 구조에 맞춰 수정
    }

    @Test
    @DisplayName("[성공] 배송지 수정 API - 200 OK")
    void updateAddressApiTest() throws Exception {
        UUID addressId = UUID.randomUUID();
        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder()
                .alias("새집").address("서울특별시 중구").build();
        ResGetAddressDtoV1 response = ResGetAddressDtoV1.builder()
                .addressId(addressId).alias("새집").build();
        
        given(addressService.updateAddress(eq(addressId), any(), eq(userId))).willReturn(response);

        mockMvc.perform(put("/api/v1/addresses/" + addressId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[실패] 타인의 배송지 수정 시도 시 403 Forbidden 반환")
    void updateAddressForbiddenTest() throws Exception {
        UUID addressId = UUID.randomUUID();
        ReqUpdateAddressDtoV1 request = ReqUpdateAddressDtoV1.builder().address("몰래수정").build();
        
        // 서비스에서 소유권 에러를 던지도록 설정
        given(addressService.updateAddress(eq(addressId), any(), eq(userId)))
                .willThrow(new CustomException(ErrorCode.ADDRESS_NOT_OWNER));

        mockMvc.perform(put("/api/v1/addresses/" + addressId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()) // 403 확인
                .andExpect(jsonPath("$.code").value("AD002"));
    }

    @Test
    @DisplayName("[성공] 배송지 삭제 API - 204 No Content")
    void deleteAddressApiTest() throws Exception {
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/addresses/" + addressId)
                        .with(user(authUser)))
                .andExpect(status().isNoContent());
    }
}
