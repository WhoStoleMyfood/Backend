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

// 3. Mockito 설정 (given, any용)
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        // 서비스는 이제 TokenResult를 줍니다.
        com.example.whostolemyfood.user.application.security.TokenResult serviceResponse =
                new com.example.whostolemyfood.user.application.security.TokenResult(TEST_USER_ID, "access-token-xyz", "refresh-token-abc");

        given(authService.login(any())).willReturn(serviceResponse);

        // 2. 실행 및 검증 (When & Then)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-xyz"))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID.toString()))
                // 리프레시 토큰은 바디에 없어야 하므로 존재하지 않는지 확인
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                // 쿠키에 리프레시 토큰이 설정되었는지 확인
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    public void logout_success() throws Exception {
        // 컨트롤러에서 AuthUser를 사용하므로, ArgumentMatchers나 Security 설정을 타야 하지만
        // addFilters = false 상태에서는 Principal이 null로 들어올 수 있습니다.
        // 이 경우 @AuthenticationPrincipal을 모킹하거나 void 메서드 호출을 확인합니다.

        // 1. 실행 및 검증
        mockMvc.perform(post("/api/v1/auth/logout")
                        // SecurityContext에 유저가 있다고 가정하거나 필터를 껐으므로
                        // 실제 AuthUser 객체 주입은 테스트 환경 설정에 따라 다를 수 있음
                        .principal(() -> TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 성공"));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    public void signout_success() throws Exception {
        // 1. 실행 및 검증
        mockMvc.perform(post("/api/v1/auth/signout")
                        .principal(() -> TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴 완료"));
    }
}
