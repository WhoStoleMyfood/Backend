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
import com.example.whostolemyfood.user.domain.entity.UserRole;
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
    @Transactional
    public ResSignUpDtoV1 signup(@Valid ReqSignUpDtoV1 requestDto) {

        // 1. [보안 검증] 가입 가능한 권한인지 체크
        // 일반 가입에서 MASTER나 MANAGER 권한을 요청하면 예외를 던집니다.
        if (requestDto.getUserRole() == UserRole.MASTER || requestDto.getUserRole() == UserRole.MANAGER) {
            throw new CustomException(ErrorCode.ACCESS_DENIED); // 혹은 적절한 에러 코드
        }

        // 2. [데이터 정합성] 중복 이메일 체크 (탈퇴하지 않은 유저 중 확인)
        // existsByUserEmailAndIsDeletedFalse 같은 메서드를 Repository에 만드는 게 좋습니다.
        if (userRepository.existsByUserEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.USER_DUPLICATION_EMAIL);
        }

        // 3. 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

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
    @Transactional
    public void signout(UUID userId) {
        // 1. 대상 조회
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        // 2. [중복 탈퇴 방지] 이미 탈퇴한 경우 체크
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. Soft Delete 수행 (누가 탈퇴시켰는지 id 기록)
        user.softDelete(userId);

        // 4. [JWT 연동 시 추가] 토큰 무효화 로직이 필요하다면 여기서 수행
        // redisRefreshTokenAdapter.deleteByUserId(userId);
    }

    @Override
    public ResLoginDtoV1 login(ReqLoginDtoV1 requestDto) {
        // 1. 유저 조회
        UserEntity user = userRepository.findByUserEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. [탈퇴 검증] 탈퇴한 회원은 로그인 불가
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND); // 보안상 '찾을 수 없음'으로 통일하기도 합니다.
        }

        // 3. 비밀번호 체크
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getUserPassword())) {
            throw new CustomException(ErrorCode.USER_WRONG_PW);
        }

        // 4. JWT 적용
        String accessToken = jwtUtil.createToken(user.getId(), user.getUserRole());
        ResLoginDtoV1 response = new ResLoginDtoV1(user.getId(), accessToken);
        return response;
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        // 1. [검증] 현재 로그아웃을 요청한 유저가 실제 존재하는지 확인
        UserEntity user = userRepository.findByIdOrElseThrow(userId);

        // 2. [검증] 이미 탈퇴한 회원인지 확인
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. [미래를 위한 자리] 레디스가 들어오면 여기서 Refresh Token을 삭제할 예정!
        // TODO: Redis 도입 후 삭제 로직 구현 (현재는 Stateless 특성상 클라이언트가 토큰을 버리는 것으로 대체)
        // redisRefreshTokenAdapter.deleteByUserId(userId);

        // 4. [기록] 보안 관례상 마지막 로그아웃 시간을 기록하기도 함
        // user.updateLastLogoutAt();
    }

}