package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.whostolemyfood.auth.application.service.AuthService;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminServiceV1 implements UserAdminService {

    private final UserRepository userRepository;
    private final AuthService authService;

    // [MASTER 전용] 매니저 생성
    @Transactional
    public ResGetUserByIdDtoV1 registerManager(ReqManagerCreateDtoV1 requestDto) {
        // 1. 매니저 생성용 회원가입 DTO로 변환
        ReqSignUpDtoV1 signUpDto = ReqSignUpDtoV1.builder()
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .userName(requestDto.getName())
                .userRole(UserRole.MANAGER)
                .adminToken(requestDto.getAdminToken())
                .build();

        // 2. AuthService.signup 재사용 (여기서 토큰 검증 로직이 실행됨)
        ResSignUpDtoV1 res = authService.signup(signUpDto);

        // 3. 반환 타입에 맞춰 변환해서 응답
        UserEntity manager = userRepository.findByUserEmail(res.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new ResGetUserByIdDtoV1(manager);
    }

    // [MASTER] 매니저 삭제 로직
    public void deleteManager(UUID userId) {
        // AuthService의 signout 기능을 재사용!
        authService.signout(userId);
    }

    // [MASTER/MANAGER] 단일 사용자 상세 조회
    public ResGetUserByIdDtoV1 getUserById(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // DTO의 엔티티 수용 생성자를 호출하여 반환
        return new ResGetUserByIdDtoV1(user);
    }

    // [MASTER] 전체 사용자 목록 조회 (본인 제외 페이징)
    public Page<ResGetUserByIdDtoV1> findAllUsers(Pageable validatedPageable, UUID myId) { // 👈 UUID myId 파라미터 추가!

        Page<UserEntity> userPage = userRepository.findAllExceptMe(validatedPageable, myId);
        return userPage.map(ResGetUserByIdDtoV1::new);
    }

//    // [MASTER] 사용자 상태 변경 (ACTIVE, DELETED 등)
//    @Transactional
//    public void updateUserStatus(UUID userId, @Valid ReqUpdateUserStatusDtoV1 requestDto) {
//        UserEntity user = userRepository.findById(userId)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        // 엔티티 내부의 비즈니스 메서드 호출
//        user.updateStatus(requestDto.getStatus());
//    }
}
