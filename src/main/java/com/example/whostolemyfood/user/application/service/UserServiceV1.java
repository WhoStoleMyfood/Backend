package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = "userCache", key = "#id")
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
    @CacheEvict(cacheNames = "userCache", key = "#id")
    public ResUpdateUserDtoV1 updateUser(UUID id, ReqUpdateUserDtoV1 requestDto) {
        UserEntity user = findActiveUser(id);

        // 1. 비밀번호가 입력된 경우에만 암호화해서 업데이트
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
            user.updatePassword(encodedPassword);
        }

        // 2. 나머지 일반 정보 업데이트
        user.updateUserInfo(requestDto);
        user.markUpdatedBy(id);

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