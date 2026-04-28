package com.example.whostolemyfood.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.whostolemyfood.global.exception.GlobalExceptionHandler;
import com.example.whostolemyfood.user.application.service.UserAdminServiceV1;
import com.example.whostolemyfood.user.presentation.controller.UserAdminControllerV1;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;

@WebMvcTest(UserAdminControllerV1.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // @PreAuthorize 검증은 실제 서버에서 하고, 여기선 로직 위주로 테스트
public class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAdminServiceV1 userAdminService;

    private final UUID 관리자_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private final UUID 대상_유저_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655441111");

    @Test
    @DisplayName("전체 사용자 목록 조회 성공 테스트 (본인 제외)")
    void 전체_사용자_조회_성공() throws Exception {
        // 1. 준비 (Given)
        ResGetUserByIdDtoV1 유저1 = ResGetUserByIdDtoV1.builder()
                .email("user1@test.com").name("유저1").role(UserRole.CUSTOMER).build();
        PageImpl<ResGetUserByIdDtoV1> 페이지_결과 = new PageImpl<>(List.of(유저1), PageRequest.of(0, 10), 1);

        given(userAdminService.findAllUsers(any(Pageable.class), any(UUID.class))).willReturn(페이지_결과);

        // 2. 실행 및 검증
        mockMvc.perform(get("/api/v1/admin/users")
                        .principal(() -> 관리자_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user1@test.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("특정 사용자 상세 조회 성공 테스트")
    void 사용자_상세_조회_성공() throws Exception {
        // 1. 준비
        ResGetUserByIdDtoV1 응답 = ResGetUserByIdDtoV1.builder()
                .email("target@test.com").name("대상유저").role(UserRole.CUSTOMER).build();

        given(userAdminService.getUserById(대상_유저_ID)).willReturn(응답);

        // 2. 실행 및 검증
        mockMvc.perform(get("/api/v1/admin/users/{userId}", 대상_유저_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("target@test.com"));
    }

    @Test
    @DisplayName("[MASTER] 매니저 생성 성공 테스트")
    void 매니저_생성_성공() throws Exception {
        // 1. 준비
        ReqManagerCreateDtoV1 요청 = new ReqManagerCreateDtoV1("manager@test.com", "pw123", "매니저", "admin-token");
        ResGetUserByIdDtoV1 응답 = ResGetUserByIdDtoV1.builder()
                .email("manager@test.com").name("매니저").role(UserRole.MANAGER).build();

        given(userAdminService.registerManager(any(ReqManagerCreateDtoV1.class))).willReturn(응답);

        // 2. 실행 및 검증
        mockMvc.perform(post("/api/v1/admin/users/managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(요청)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    @DisplayName("[MASTER] 매니저 삭제 성공 테스트")
    void 매니저_삭제_성공() throws Exception {
        // 1. 실행 및 검증
        mockMvc.perform(delete("/api/v1/admin/users/managers/{userId}", 대상_유저_ID)
                        .principal(() -> 관리자_ID.toString()))
                .andExpect(status().isNoContent());

        verify(userAdminService, times(1)).deleteManager(대상_유저_ID);
    }

    @Test
    @DisplayName("본인 계정 삭제 시도 시 예외 발생 테스트")
    void 본인_삭제_차단_테스트() throws Exception {
        // 2. 실행 및 검증 (본인의 ID로 삭제 요청)
        mockMvc.perform(delete("/api/v1/admin/users/managers/{userId}", 관리자_ID)
                        .principal(() -> 관리자_ID.toString()))
                .andExpect(status().isForbidden()); // 에러 코드에 따라 status는 달라질 수 있음 (GlobalExceptionHandler 기준)
    }
}