package com.example.whostolemyfood.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.whostolemyfood.auth.application.service.AuthServiceV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthServiceV1 authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private final String 가짜_어드민_토큰 = "test-admin-secret";

    @BeforeEach
    void 설정() {

        ReflectionTestUtils.setField(authService, "adminSecretKey", 가짜_어드민_토큰);
    }

    @Test
    @DisplayName("회원가입 성공 - 일반 고객(CUSTOMER) 권한")
    void 회원가입_성공() {
        // 1. 준비
        ReqSignUpDtoV1 요청 = ReqSignUpDtoV1.builder()
                .email("soyoon@test.com")
                .password("password123")
                .userName("소윤")
                .userRole(UserRole.CUSTOMER)
                .build();

        given(userRepository.existsByUserEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded-pw");

        // 2. 실행
        ResSignUpDtoV1 결과 = authService.signup(요청);

        // 3. 검증
        assertThat(결과.getEmail()).isEqualTo("soyoon@test.com");
        assertThat(결과.getUserName()).isEqualTo("소윤");
        // 실제로 DB 저장 메서드가 호출되었는지 확인
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void 회원가입_실패_중복_이메일() {
        // 1. 준비
        ReqSignUpDtoV1 요청 = ReqSignUpDtoV1.builder()
                .email("duplicate@test.com")
                .userRole(UserRole.CUSTOMER)
                .build();

        given(userRepository.existsByUserEmail(anyString())).willReturn(true);

        // 2. 실행 및 검증
        assertThatThrownBy(() -> authService.signup(요청))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_DUPLICATION_EMAIL);
    }

    @Test
    @DisplayName("로그인 성공 - 토큰 발급 확인")
    void 로그인_성공() {
        // 1. 준비
        UUID 유저_ID = UUID.randomUUID();
        ReqLoginDtoV1 요청 = new ReqLoginDtoV1("soyoon@test.com", "password123");

        UserEntity 가짜_유저 = UserEntity.builder()
                .email("soyoon@test.com")
                .password("encoded-pw")
                .role(UserRole.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(가짜_유저, "id", 유저_ID);

        given(userRepository.findByUserEmail(anyString())).willReturn(Optional.of(가짜_유저));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createToken(any(UUID.class), any(UserRole.class))).willReturn("fake-access-token");

        // 2. 실행
        ResLoginDtoV1 결과 = authService.login(요청);

        // 3. 검증
        assertThat(결과.getUserId()).isEqualTo(유저_ID);
        assertThat(결과.getAccessToken()).isEqualTo("fake-access-token");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void 로그인_실패_비밀번호_틀림() {
        // 1. 준비
        ReqLoginDtoV1 요청 = new ReqLoginDtoV1("soyoon@test.com", "wrong-pw");
        UserEntity 가짜_유저 = UserEntity.builder()
                .email("soyoon@test.com")
                .password("encoded-pw")
                .build();

        given(userRepository.findByUserEmail(anyString())).willReturn(Optional.of(가짜_유저));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // 2. 실행 및 검증
        assertThatThrownBy(() -> authService.login(요청))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_WRONG_PW);
    }

    @Test
    @DisplayName("회원가입 실패 - 관리자 토큰 불일치 (MASTER 권한 시도)")
    void 회원가입_실패_어드민토큰_오류() {
        // 1. 준비
        ReqSignUpDtoV1 요청 = ReqSignUpDtoV1.builder()
                .email("master@test.com")
                .userRole(UserRole.MASTER)
                .adminToken("wrong-secret-key") // 잘못된 토큰 입력
                .build();

        // 2. 실행 및 검증
        assertThatThrownBy(() -> authService.signup(요청))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }
}