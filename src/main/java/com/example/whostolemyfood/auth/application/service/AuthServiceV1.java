package com.example.whostolemyfood.auth.application.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.application.security.TokenResult;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.admin-token}")
    private String adminSecretKey;

    @Override
    @Transactional
    public ResSignUpDtoV1 signup(@Valid ReqSignUpDtoV1 requestDto) {
        // 기존 디버깅용 출력 유지
        System.out.println("--- 회원가입 디버깅 ---");
        System.out.println("서버 AdminKey: " + adminSecretKey);
        System.out.println("포스트맨 AdminToken: " + requestDto.getAdminToken());

        UserRole requestedRole = requestDto.getUserRole();

        // 1. 매니저 가입 차단
        if (requestedRole == UserRole.MANAGER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 1-2. 마스터 권한 확인
        if (requestedRole == UserRole.MASTER) {
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

    @Transactional
    @Override
    public TokenResult login(ReqLoginDtoV1 requestDto) {
        UserEntity user = userRepository.findByUserEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PW);
        }

        // 토큰 생성 및 Redis 저장
        String accessToken = jwtUtil.createToken(user.getId(), user.getUserRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set("RT:" + user.getId(), refreshToken, 14, TimeUnit.DAYS); // 2주 설정

        return new TokenResult(user.getId(), accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. Redis 블랙리스트 등록
        String redisKey = "logout:" + userId.toString();
        redisTemplate.opsForValue().set(redisKey, "true", 30, TimeUnit.MINUTES);

        // 2. [추가] 로그아웃 시 리프레시 토큰도 삭제 (보안상 필수)
        redisTemplate.delete("RT:" + userId.toString());

        System.out.println("Redis 로그아웃 처리 완료: " + redisKey);
    }


    @Transactional
    @Override
    public TokenResult reissue(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        UUID userId = UUID.fromString(jwtUtil.extractSubject(refreshToken));
        String redisKey = "RT:" + userId;
        String savedToken = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        // RTR 적용: 새 리프레시 토큰 생성 및 Redis 갱신
        String newAccessToken = jwtUtil.createToken(user.getId(), user.getUserRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(redisKey, newRefreshToken, 14, TimeUnit.DAYS);

        return new TokenResult(user.getId(), newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void signout(UUID userId) {
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        user.softDelete(userId);
        // 탈퇴 시에도 토큰 삭제
        redisTemplate.delete("RT:" + userId.toString());
    }
}