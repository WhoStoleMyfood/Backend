package com.example.whostolemyfood.auth.application.service;

import java.util.UUID;
import jakarta.validation.Valid;

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
import com.example.whostolemyfood.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 기본 설정
public class AuthServiceV1 implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // private final RedisRefreshTokenAdapter redisRefreshTokenAdapter; // 주석 해제 필요 시 추가

    @Override
    @Transactional // 쓰기 작업이므로 트랜잭션 추가
    public ResSignUpDtoV1 signup(@Valid ReqSignUpDtoV1 requestDto) {

        // 1. 이메일 중복 검증 (필드명 userEmail에 맞춤)
        if (userRepository.existsByUserEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.USER_DUPLICATION_EMAIL);
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. 빌더 패턴을 사용한 유저 데이터 저장
        UserEntity user = UserEntity.builder()
                .role(requestDto.getUserRole())
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .name(requestDto.getUserName())
                .build();

        UserEntity savedUser = userRepository.save(user);

        return new ResSignUpDtoV1(savedUser.getUserEmail(), savedUser.getUserName());
    }

    @Override
    public ResLoginDtoV1 login(ReqLoginDtoV1 requestDto) {
        UserEntity user = userRepository.findByUserEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 깔끔!

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PW);
        }

        return new ResLoginDtoV1(user.getId());
    }

    @Override
    @Transactional
    public void logout(UUID userId) {

        userRepository.findByIdOrElseThrow(userId);

    }

    @Override
    public void signout(UUID userId) {
        // 회원 탈퇴 로직 (Soft Delete)
        UserEntity user = userRepository.findByIdOrElseThrow(userId);
        user.softDelete(); // BaseSoftDeleteEntity의 메서드 활용
    }
}