package com.example.whostolemyfood.auth.presentation.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.whostolemyfood.auth.application.service.AuthServiceV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResReissueDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.request.ReqSignUpDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResLoginDtoV1;
import com.example.whostolemyfood.auth.presentation.dto.response.ResSignUpDtoV1;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.application.security.TokenResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @PostMapping("/login")
    public ResponseEntity<ResLoginDtoV1> login(
            @RequestBody ReqLoginDtoV1 requestDto,
            HttpServletResponse response
    ) {
        TokenResult result = authServiceV1.login(requestDto);

        // 쿠키 설정 시 getRefreshToken() 사용
        setRefreshTokenCookie(response, result.getRefreshToken());

        // DTO 반환 시 getUserId(), getAccessToken() 사용
        return ResponseEntity.ok(new ResLoginDtoV1(result.getUserId(), result.getAccessToken()));
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
    public ResponseEntity<ResReissueDtoV1> reissue(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        TokenResult result = authServiceV1.reissue(refreshToken);

        // 쿠키 갱신 시 getRefreshToken() 사용
        setRefreshTokenCookie(response, result.getRefreshToken());

        // DTO 반환 시 getAccessToken() 사용
        return ResponseEntity.ok(new ResReissueDtoV1(result.getAccessToken()));
    }

    // 쿠키 설정 공통 메서드
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)    // HTTPS 환경에서 필수
                .path("/")
                .maxAge(14 * 24 * 60 * 60) // 2주
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}