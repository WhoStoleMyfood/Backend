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

    @Override
    @Transactional
    public ResLoginDtoV1 login(ReqLoginDtoV1 requestDto) {
        UserEntity user = userRepository.findByUserEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // [추가] 비밀번호 검증 로직 (이게 빠져있으면 아무나 로그인됩니다!)
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PW);
        }

        // 1. 토큰 생성
        String accessToken = jwtUtil.createToken(user.getId(), user.getUserRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 2. Redis에 Refresh Token 저장 (7일 TTL)
        String redisKey = "RT:" + user.getId().toString();
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);

        // [수정] DTO 생성 시 세 번째 인자로 refreshToken 전달
        return new ResLoginDtoV1(user.getId(), accessToken, refreshToken);
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
    public ResLoginDtoV1 reissue(String refreshToken) { // 반환 타입을 DTO로 변경
        // 1. 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 사용자 ID 추출
        String userIdString = jwtUtil.extractSubject(refreshToken);
        UUID userId = UUID.fromString(userIdString);

        // 3. Redis 대조 (생략 가능하지만 보안상 유지)
        String redisKey = "RT:" + userIdString;
        String savedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 4. 유저 상태 확인 (여기서 엔티티를 어차피 조회함!)
        UserEntity user = userRepository.findByIdOrElseThrow(userId);
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 5. 새 액세스 토큰 생성
        String newAccessToken = jwtUtil.createToken(user.getId(), user.getUserRole());

        // 서비스에서 DTO를 완성해서 보냄 (엔티티의 ID를 바로 사용)
        return new ResLoginDtoV1(user.getId(), newAccessToken, refreshToken);
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