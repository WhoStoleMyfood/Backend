package com.example.whostolemyfood.auth.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.whostolemyfood.auth.application.service.AuthServiceV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResSignUpDtoV1> signUp(@Valid @RequestBody ReqSignUpDtoV1 requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServiceV1.signup(requestDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ResLoginDtoV1> login(@Valid @RequestBody ReqLoginDtoV1 requestDto) {
        return ResponseEntity.ok(authServiceV1.login(requestDto));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal AuthUser loginUser) { // UserEntity -> AuthUser
        authServiceV1.logout(loginUser.userId());
        return ResponseEntity.ok("로그아웃 성공");
    }

    // 회원 탈퇴
    @PostMapping("/signout")
    public ResponseEntity<String> signout(@AuthenticationPrincipal AuthUser loginUser) { // UserEntity -> AuthUser
        authServiceV1.signout(loginUser.userId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }
}