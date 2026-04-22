package com.example.whostolemyfood.user.presentation.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.whostolemyfood.user.application.service.UserService;
import com.example.whostolemyfood.user.presentation.dto.request.ReqUpdateUserDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users") // 관례상 복수형(users)을 많이 사용합니다.
public class UserControllerV1 {

    private final UserService userService; // 인터페이스를 주입받는 것이 유연합니다.

    /**
     * 회원 단건 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ResGetUserByIdDtoV1> getUserById(@PathVariable UUID userId) {
        // 서비스 호출
        ResGetUserByIdDtoV1 response = userService.getUserById(userId);

        // 성공 시 200 OK와 함께 데이터 반환
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보 수정
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody ReqUpdateUserDtoV1 requestDto
    ) {
        // 서비스 호출 (수정 로직 수행)
        userService.updateUser(userId, requestDto);

        // 수정 완료 시 별도의 바디 없이 200 OK 혹은 204 No Content 반환
        return ResponseEntity.ok().build();
    }
}