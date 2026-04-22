package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.repository.UserRepository;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회가 많으므로 기본은 readOnly
public class UserServiceV1 implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResGetUserByIdDtoV1 getUserById(UUID id) {
        // 1. [검증 및 예외 발생] 로직의 가장 상단에서 "입구 컷"을 합니다.
        // 존재하지 않는 데이터로 이후 로직이 실행되지 않도록 즉시 예외를 던집니다.
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. [비즈니스 예외 처리] 데이터는 있지만, 논리적으로 에러인 상황을 체크합니다.
        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND); // 탈퇴 유저는 없는 유저로 취급
        }

        // 3. [정상 흐름 반환] 모든 검증을 통과했을 때만 결과를 변환해 나갑니다.
        ResGetUserByIdDtoV1 response = new ResGetUserByIdDtoV1(
                user.getUserEmail(),
                user.getUserName(),
                user.getUserRole()
        );

        return response;
    }

    @Override
    @Transactional
    public void updateUser(UUID id, ReqUpdateUserDtoV1 requestDto) {
        // 1. [검증] 대상 존재 확인
        // 조회가 안 되면 CustomException(USER_NOT_FOUND)이 여기서 바로 터집니다.
        UserEntity user = userRepository.findByIdOrElseThrow(id);

        // 2. [검증] 비즈니스 로직 예외 처리
        // 예: 탈퇴한 회원은 수정할 수 없도록 방어 로직 추가
        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. [가공] 비밀번호 암호화
        // 비밀번호가 들어왔을 때만 암호화 로직을 수행합니다.
        String encodedPassword = null;
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        }

        // 4. [행위] 엔티티 상태 변경
        // 엔티티 내부 메서드를 통해 안전하게 값을 변경합니다.
        user.updateUserInfo(
                requestDto.getUserName(),
                encodedPassword
        );

        // 5. [기록] Audit 정보 갱신
        // BaseAuditEntity의 메서드를 활용해 수정자 정보를 남깁니다.
        user.markUpdatedBy(id);

        // 별도의 return 없이 트랜잭션 종료 시 Dirty Checking으로 DB 반영!
    }
}