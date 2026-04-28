package com.example.whostolemyfood.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.user.application.service.UserService;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.presentation.controller.UserControllerV1;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResUpdateUserDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;

@WebMvcTest(UserControllerV1.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터는 일단 끄고 진행합니다.
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // 테스트에 사용할 고정 UUID
    private final UUID 테스트_유저_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    @DisplayName("내 정보 조회 성공 테스트 - 이메일, 이름, 역할 확인")
    void 내_정보_조회_성공() throws Exception {
        // 1. 준비 (Given)
        ResGetUserByIdDtoV1 응답_DTO = new ResGetUserByIdDtoV1(
                "whostolemyfood@test.com",
                "whostolemyfood1",
                com.example.whostolemyfood.user.domain.entity.UserRole.CUSTOMER
        );

        given(userService.getUserById(any(UUID.class))).willReturn(응답_DTO);

        // 2. 실행 (When) & 검증 (Then)
        mockMvc.perform(get("/api/v1/user/me")
                        .principal(() -> 테스트_유저_ID.toString()))
                .andExpect(status().isOk())
                // JSON 응답 필드 검증
                .andExpect(jsonPath("$.email").value("whostolemyfood@test.com"))
                .andExpect(jsonPath("$.name").value("whostolemyfood"))
                .andExpect(jsonPath("$.role").value("CUSTOMER")); // Enum은 문자열로 비교
    }

    @Test
    @DisplayName("내 정보 수정 성공 테스트 - 이름, 비밀번호, 주소 변경")
    void 내_정보_수정_성공() throws Exception {
        // 1. 준비
        ReqUpdateUserDtoV1 요청_DTO = new ReqUpdateUserDtoV1();
        org.springframework.test.util.ReflectionTestUtils.setField(요청_DTO, "name", "whostolemyfood수정수정");
        org.springframework.test.util.ReflectionTestUtils.setField(요청_DTO, "password", "new-password-123");
        org.springframework.test.util.ReflectionTestUtils.setField(요청_DTO, "address", "서울시 강남구");

        // 서비스 응답 가짜 데이터 생성
        ResUpdateUserDtoV1 응답_DTO = ResUpdateUserDtoV1.builder()
                .email("soyoon@test.com")
                .name("whostolemyfood수정수정")
                .role(com.example.whostolemyfood.user.domain.entity.UserRole.CUSTOMER)
                .message("회원 정보 및 주소가 수정되었습니다.")
                .build();

        given(userService.updateUser(any(UUID.class), any(ReqUpdateUserDtoV1.class)))
                .willReturn(응답_DTO);

        // 2. 실행 (When) & 검증 (Then)
        mockMvc.perform(patch("/api/v1/user/me")
                        .principal(() -> 테스트_유저_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청_DTO)))
                .andExpect(status().isOk())
                // JSON 응답 필드 검증
                .andExpect(jsonPath("$.email").value("whostolemyfood@test.com"))
                .andExpect(jsonPath("$.name").value("whostolemyfood수정수정"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.message").value("회원 정보 및 주소가 수정되었습니다."));
    }
}