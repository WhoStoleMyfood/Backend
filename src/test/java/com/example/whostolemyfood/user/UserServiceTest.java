package com.example.whostolemyfood.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.application.service.UserServiceV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResUpdateUserDtoV1;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceV1 userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UUID 테스트_유저_ID = UUID.randomUUID();

    @Test
    @DisplayName("유저 조회 성공 - 활성 상태 유저")
    void 유저_조회_성공() {
        // 1. 준비
        UserEntity 가짜_유저 = UserEntity.builder()
                .email("whostolemyFood.com")
                .name("whostolemyFood1")
                .role(UserRole.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(가짜_유저, "isDeleted", false);

        given(userRepository.findById(테스트_유저_ID)).willReturn(Optional.of(가짜_유저));

        // 2. 실행 (When)
        ResGetUserByIdDtoV1 결과 = userService.getUserById(테스트_유저_ID);

        // 3. 검증 (Then)
        assertThat(결과.getEmail()).isEqualTo("whostolemyFood@test.com");
        assertThat(결과.getName()).isEqualTo("whostolemyFood1");
    }

    @Test
    @DisplayName("유저 조회 실패 - 삭제된 유저인 경우")
    void 유저_조회_실패_삭제됨() {
        // 1. 준비
        UserEntity 삭제된_유저 = UserEntity.builder().build();
        ReflectionTestUtils.setField(삭제된_유저, "isDeleted", true); // 삭제 상태로 세팅

        given(userRepository.findById(테스트_유저_ID)).willReturn(Optional.of(삭제된_유저));

        // 2. 실행 및 검증
        assertThatThrownBy(() -> userService.getUserById(테스트_유저_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 정보 수정 성공 - 비밀번호 포함 변경")
    void 유저_정보_수정_성공() {
        // 1. 준비
        ReqUpdateUserDtoV1 요청 = new ReqUpdateUserDtoV1();
        ReflectionTestUtils.setField(요청, "name", "whostolemyFood수정");
        ReflectionTestUtils.setField(요청, "password", "new-pw");
        ReflectionTestUtils.setField(요청, "address", "서울시 강남구");

        UserEntity 기존_유저 = UserEntity.builder()
                .email("whostolemyFood@test.com")
                .name("whostolemyFood1")
                .password("old-pw")
                .role(UserRole.CUSTOMER)
                .build();
        ReflectionTestUtils.setField(기존_유저, "isDeleted", false);

        given(userRepository.findById(테스트_유저_ID)).willReturn(Optional.of(기존_유저));
        given(passwordEncoder.encode("new-pw")).willReturn("encoded-new-pw");

        // 2. 실행
        ResUpdateUserDtoV1 결과 = userService.updateUser(테스트_유저_ID, 요청);

        // 3. 검증
        assertThat(결과.getName()).isEqualTo("whostolemyFood수정수정");
        assertThat(결과.getMessage()).contains("성공적으로 수정");
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("유저 정보 수정 - 비밀번호 미포함 시 인코딩 건너뛰기")
    void 유저_수정_비밀번호_없음() {
        // 1. 준비
        ReqUpdateUserDtoV1 요청 = new ReqUpdateUserDtoV1();
        ReflectionTestUtils.setField(요청, "name", "이름만수정");
        // password는 세팅 안함 (null 혹은 공백)

        UserEntity 기존_유저 = UserEntity.builder().role(UserRole.CUSTOMER).build();
        ReflectionTestUtils.setField(기존_유저, "isDeleted", false);

        given(userRepository.findById(테스트_유저_ID)).willReturn(Optional.of(기존_유저));

        // 2. 실행
        userService.updateUser(테스트_유저_ID, 요청);

        // 3. 검증
        // passwordEncoder.encode 가 한 번도 호출되지 않아야 함
        verify(passwordEncoder, never()).encode(anyString());
    }
}