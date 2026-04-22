package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

public interface UserService {

    // 마이페이지 등을 위한 정보 조회
    ResGetUserByIdDtoV1 getUserById(UUID id);

    // 닉네임, 이메일 등 정보 수정
    void updateUser(UUID id, ReqUpdateUserDtoV1 requestDto);
}
