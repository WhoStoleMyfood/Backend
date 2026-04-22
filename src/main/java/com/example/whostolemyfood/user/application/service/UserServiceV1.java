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
import com.example.whostolemyfood.user.presentation.dto.response.ResUpdateUserDtoV1;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV1 implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResGetUserByIdDtoV1 getUserById(UUID id) {
        UserEntity user = findActiveUser(id);
        return new ResGetUserByIdDtoV1(
                user.getUserEmail(),
                user.getUserName(),
                user.getUserRole()
        );
    }

    @Override
    @Transactional
    public ResUpdateUserDtoV1 updateUser(UUID id, ReqUpdateUserDtoV1 requestDto) {
        UserEntity user = findActiveUser(id);

        // 비밀번호 변경 로직
        String encodedPassword = user.getUserPassword();
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        }

        // 도메인 모델에 업데이트 위임
        user.updateUserInfo(requestDto.getUserName(), encodedPassword);
        user.markUpdatedBy(id);

        // 서비스 단에서 DTO로 변환하여 반환 (엔티티 유출 방지)
        return new ResUpdateUserDtoV1(
                user.getUserEmail(),
                user.getUserName(),
                user.getUserRole(),
                "회원 정보가 성공적으로 수정되었습니다."
        );
    }

    private UserEntity findActiveUser(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}