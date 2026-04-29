package com.example.whostolemyfood.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import com.example.whostolemyfood.auth.presentation.dto.request.ReqReissueDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.global.config.security.jwt.JwtUtil;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@Tag(name = "Auth API", description = "계정 권한 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;

    // 회원가입
    @Operation(summary = "회원가입",description = "계정을 생성하고 시스템 이용 권한을 부여합니다")
    @PostMapping("/signup")
    public ResponseEntity<ResSignUpDtoV1> signUp(@Valid @RequestBody ReqSignUpDtoV1 requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServiceV1.signup(requestDto));
    }

    // 로그인
    @Operation(summary = "로그인",description = "Access 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<ResLoginDtoV1> login(@Valid @RequestBody ReqLoginDtoV1 requestDto) {
        return ResponseEntity.ok(authServiceV1.login(requestDto));
    }

    // 로그아웃
    @Operation(summary = "로그아웃",description = "로그아웃합니다")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal AuthUser loginUser) { // UserEntity -> AuthUser
        authServiceV1.logout(loginUser.userId());
        return ResponseEntity.ok("로그아웃 성공");
    }

    // 회원 탈퇴
    @Operation(summary = "회원탈퇴",description = "계정을 삭제(isDelete)합니다")
    @PostMapping("/signout")
    public ResponseEntity<String> signout(@AuthenticationPrincipal AuthUser loginUser) { // UserEntity -> AuthUser
        authServiceV1.signout(loginUser.userId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResLoginDtoV1> reissue(@RequestBody ReqReissueDtoV1 requestDto) {
        // 서비스가 ID, AccessToken, RefreshToken이 다 담긴 DTO를 넘겨줌
        ResLoginDtoV1 response = authServiceV1.reissue(requestDto.getRefreshToken());

        return ResponseEntity.ok(response);
    }
}