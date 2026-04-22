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
import com.example.whostolemyfood.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth") // 버전 관리와 경로 명확화
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;

    // 1. 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ResSignUpDtoV1> signUp(@Valid @RequestBody ReqSignUpDtoV1 requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServiceV1.signup(requestDto));
    }

    // 2. 로그인
    @PostMapping("/login")
    public ResponseEntity<ResLoginDtoV1> login(@Valid @RequestBody ReqLoginDtoV1 requestDto) {
        // 서비스에서 로그인 검증 및 토큰 발급 로직 처리 권장
        ResLoginDtoV1 responseDto = authServiceV1.login(requestDto);

        // 만약 헤더에 토큰을 실어 보내고 싶다면 JwtUtil을 여기서 사용하거나 서비스에서 처리한 후 헤더에 추가
        return ResponseEntity.ok(responseDto);
    }

    // 3. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserEntity loginUser) {
        // @AuthenticationPrincipal로 현재 로그인된 유저 정보를 바로 받아올 수 있습니다.
        authServiceV1.logout(loginUser.getId());
        return ResponseEntity.ok("로그아웃 성공");
    }

    // 4. 회원 탈퇴 (Soft Delete)
    @PostMapping("/signout")
    public ResponseEntity<String> signout(@AuthenticationPrincipal UserEntity loginUser) {
        authServiceV1.signout(loginUser.getId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }
}