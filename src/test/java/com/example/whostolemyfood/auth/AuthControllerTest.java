package com.example.whostolemyfood.auth;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.whostolemyfood.auth.application.service.AuthServiceV1;
import com.example.whostolemyfood.auth.presentation.controller.AuthControllerV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

// 1. 요청 빌더 (post용)
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// 2. 결과 검증 (status, jsonPath용)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// 3. Mockito 설정 (given, any용)
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(AuthControllerV1.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServiceV1 authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // 테스트용 고정 UUID
    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    @DisplayName("로그인 성공 테스트")
    public void login_success() throws Exception {
        // 1. 준비 (Given)
        ReqLoginDtoV1 request = new ReqLoginDtoV1("test@email.com", "password123");
        ResLoginDtoV1 response = new ResLoginDtoV1(TEST_USER_ID,"access-token-xyz");

        // AuthService가 어떤 이메일/비번을 받든 가짜 응답을 주도록 설정
        given(authService.login(any())).willReturn(response);

        // 2. 실행 및 검증 (When & Then)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
        // 수정 후 (실제 응답 필드명인 accessToken으로 변경)
                .andExpect(jsonPath("$.accessToken").value("access-token-xyz"))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()));
    }

    @Test
    public void logout() throws Exception {}
}
