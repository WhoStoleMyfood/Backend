package com.example.whostolemyfood.auth.application.service;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // application.yml의 auth.admin-token 값을 읽어오기
    @Value("${auth.admin-token}")
    private String adminSecretKey;

    @Override
    @Transactional
    public ResSignUpDtoV1 signup(@Valid ReqSignUpDtoV1 requestDto) {

        System.out.println("--- 회원가입 디버깅 ---");
        System.out.println("서버 AdminKey: " + adminSecretKey);
        System.out.println("포스트맨 AdminToken: " + requestDto.getAdminToken());

        // 1. 권한별 가입 제한 로직
        UserRole requestedRole = requestDto.getUserRole();

        if (requestedRole == UserRole.MASTER || requestedRole == UserRole.MANAGER) {
            // 하드코딩 대신 주입받은 adminSecretKey와 비교합니다.
            if (adminSecretKey == null || !adminSecretKey.equals(requestDto.getAdminToken())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByUserEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.USER_DUPLICATION_EMAIL);
        }

        // 3. 저장 로직
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        UserEntity user = UserEntity.builder()
                .role(requestedRole)
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .name(requestDto.getUserName())
                .build();

        userRepository.save(user);

        return new ResSignUpDtoV1(user.getUserEmail(), user.getUserName());
    }

    @Override
    @Transactional
    public void signout(UUID userId) {
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        user.softDelete(userId);
    }

    @Override
    public ResLoginDtoV1 login(ReqLoginDtoV1 requestDto) {
        UserEntity user = userRepository.findByUserEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PW);
        }

        // 토큰 생성 시 user.getId()와 user.getUserRole() 사용
        String accessToken = jwtUtil.createToken(user.getId(), user.getUserRole());
        return new ResLoginDtoV1(user.getId(), accessToken);
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        // 나중에 Redis 블랙리스트 처리 시 userId를 사용합니다.
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        // TODO: Redis 로직 추가 예정
    }
}