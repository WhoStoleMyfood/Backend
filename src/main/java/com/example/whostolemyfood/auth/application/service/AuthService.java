package com.example.whostolemyfood.auth.application.service;

import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;

public interface AuthService {

    //유저 등록 및 삭제
    ResSignUpDtoV1 signup(ReqSignUpDtoV1 requestDto);

    @Transactional
    ResLoginDtoV1 reissue(String refreshToken);

    void signout(UUID userId);

    //유저 로그인 및 로그아웃
    ResLoginDtoV1 login(ReqLoginDtoV1 requestDto);
    void logout(UUID userId);



}
