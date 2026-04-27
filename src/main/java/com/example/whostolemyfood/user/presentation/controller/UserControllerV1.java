package com.example.whostolemyfood.user.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.application.service.UserService;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResUpdateUserDtoV1;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserControllerV1 {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ResGetUserByIdDtoV1> getMyInfo(
            @AuthenticationPrincipal AuthUser loginUser // 1. 타입 변경!
    ) {
        // 2. loginUser.userId()로 안전하게 ID를 꺼내서 서비스에 넘깁니다.
        return ResponseEntity.ok(userService.getUserById(loginUser.userId()));
    }

    //

    @PatchMapping("/me")
    public ResponseEntity<ResUpdateUserDtoV1> updateMyInfo(
            @AuthenticationPrincipal AuthUser loginUser,
            @Valid @RequestBody ReqUpdateUserDtoV1 requestDto
    ) {

        ResUpdateUserDtoV1 response = userService.updateUser(loginUser.userId(), requestDto);
        return ResponseEntity.ok(response);
    }

}