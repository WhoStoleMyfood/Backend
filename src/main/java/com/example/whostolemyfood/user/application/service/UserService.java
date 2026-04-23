package com.example.whostolemyfood.user.application.service;

import java.util.UUID;

import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResUpdateUserDtoV1;

public interface UserService {
    // 마이페이지 등을 위한 정보 조회
    ResGetUserByIdDtoV1 getUserById(UUID id);

    // 정보 수정 후 결과를 DTO로 반환
    ResUpdateUserDtoV1 updateUser(UUID id, ReqUpdateUserDtoV1 requestDto);
}
