package com.example.whostolemyfood.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.whostolemyfood.auth.application.service.AuthService;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.user.application.service.UserAdminServiceV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    @InjectMocks
    private UserAdminServiceV1 userAdminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    private final UUID 관리자_ID = UUID.randomUUID();
    private final UUID 대상_유저_ID = UUID.randomUUID();

    @Test
    @DisplayName("매니저 등록 성공 - AuthService 재사용 확인")
    void 매니저_등록_성공() {
        ReqManagerCreateDtoV1 요청 = new ReqManagerCreateDtoV1("manager@test.com", "pw123", "매니저", "admin-token");
        ResSignUpDtoV1 인증서비스_응답 = new ResSignUpDtoV1("manager@test.com", "매니저");

        UserEntity 저장된_매니저 = UserEntity.builder()
                .email("manager@test.com")
                .name("매니저")
                .role(UserRole.MANAGER)
                .build();

        given(authService.signup(any())).willReturn(인증서비스_응답);
        given(userRepository.findByUserEmail(anyString())).willReturn(Optional.of(저장된_매니저));

        ResGetUserByIdDtoV1 결과 = userAdminService.registerManager(요청);

        assertThat(결과.getEmail()).isEqualTo("manager@test.com");
        assertThat(결과.getRole()).isEqualTo(UserRole.MANAGER);
        verify(authService, times(1)).signup(any());
    }

    @Test
    @DisplayName("전체 사용자 목록 조회 - 본인 제외 페이징 확인")
    void 전체_사용자_조회_성공() {
        Pageable 페이지_요청 = PageRequest.of(0, 10);
        UserEntity 유저1 = UserEntity.builder().email("user1@test.com").name("유저1").role(UserRole.CUSTOMER).build();
        Page<UserEntity> 페이지_결과 = new PageImpl<>(List.of(유저1), 페이지_요청, 1);

        // 💡 인자 추가: UserRole.MASTER (조회 주체의 권한)
        given(userRepository.findAllExceptMe(페이지_요청, 관리자_ID)).willReturn(페이지_결과);

        // 💡 실행 시 권한 인자 추가
        Page<ResGetUserByIdDtoV1> 결과 = userAdminService.findAllUsers(페이지_요청, 관리자_ID, UserRole.MASTER);

        assertThat(결과.getContent()).hasSize(1);
        assertThat(결과.getContent().get(0).getEmail()).isEqualTo("user1@test.com");
        verify(userRepository, times(1)).findAllExceptMe(any(), any());
    }

    @Test
    @DisplayName("매니저 삭제 - AuthService.signout 호출 확인")
    void 매니저_삭제_성공() {
        userAdminService.deleteManager(대상_유저_ID);
        verify(authService, times(1)).signout(대상_유저_ID);
    }

    @Test
    @DisplayName("사용자 상세 조회 성공")
    void 사용자_상세_조회_성공() {
        UserEntity 유저 = UserEntity.builder()
                .email("detail@test.com")
                .name("상세유저")
                .role(UserRole.CUSTOMER)
                .build();
        given(userRepository.findById(대상_유저_ID)).willReturn(Optional.of(유저));

        // 💡 실행 시 권한 인자 추가 (MASTER 권한으로 조회한다고 가정)
        ResGetUserByIdDtoV1 결과 = userAdminService.getUserById(대상_유저_ID, UserRole.MASTER);

        assertThat(결과.getEmail()).isEqualTo("detail@test.com");
        assertThat(결과.getName()).isEqualTo("상세유저");
    }
}