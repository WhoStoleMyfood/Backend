package com.example.whostolemyfood.user.presentation.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.whostolemyfood.global.exception.CustomException;
import com.example.whostolemyfood.global.exception.ErrorCode;
import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.user.application.security.AuthUser;
import com.example.whostolemyfood.user.application.service.UserAdminServiceV1;
import com.example.whostolemyfood.user.presentation.dto.request.ReqManagerCreateDtoV1;
import com.example.whostolemyfood.user.presentation.dto.response.ResGetUserByIdDtoV1;

import lombok.RequiredArgsConstructor;

@Tag(name = "UserAdmin API", description = "관리자 유저 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class UserAdminControllerV1 {

    private final UserAdminServiceV1 userAdminService;

    //1. 전체 사용자 목록 조회 (본인 제외)
    @Operation(summary = "사용자 전체 조회(본인 제외)", description = "[MANAGER / MASTER] 관리자가 전체 사용자 목록을 조회합니다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<PageResponse<ResGetUserByIdDtoV1>> getAllUsers(
            Pageable pageable,
            @AuthenticationPrincipal AuthUser loginUser
    ) {
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);

        // role 정보를 함께 넘겨줍니다.
        Page<ResGetUserByIdDtoV1> userPage = userAdminService.findAllUsers(
                validatedPageable, loginUser.userId(), loginUser.role());
        return ResponseEntity.ok(new PageResponse<>(userPage));
    }

    // 2. 특정 사용자 상세 정보 조회
    @Operation(summary = "특정 사용자 조회", description =  "[MANAGER / MASTER] 관리자가 특정 사용자를 조회합니다.")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ResGetUserByIdDtoV1> getUserDetail(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthUser loginUser // 👈 추가
    ) {
        // role 정보를 함께 넘겨줍니다.
        return ResponseEntity.ok(userAdminService.getUserById(userId, loginUser.role()));
    }

//    //3. 사용자 상태 관리-API 호출로 직접 본인을 변경하는 것을 방지하는 2중 보안
//    @PatchMapping("/{userId}/status")
//    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
//    public ResponseEntity<Void> updateUserStatus(
//            @PathVariable UUID userId,
//            @Valid @RequestBody ReqUpdateUserStatusDtoV1 requestDto,
//            @AuthenticationPrincipal AuthUser loginUser
//    ) {
//        // 본인 계정 상태 변경 시도 차단
//        if (userId.equals(loginUser.userId())) {
//            throw new CustomException(ErrorCode.SELF_RESOURCE_ACCESS_DENIED);
//        }
//
//        userAdminService.updateUserStatus(userId, requestDto);
//        return ResponseEntity.noContent().build();
//    }

    // 4. [MASTER 전용] 매니저 생성
    @Operation(summary = "매니저 임명", description = "[MASTER] 관리자가 매니저를 임명합니다.")
    @PostMapping("/managers")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<ResGetUserByIdDtoV1> createManager(
            @Valid @RequestBody ReqManagerCreateDtoV1 requestDto
    ) {
        return ResponseEntity.ok(userAdminService.registerManager(requestDto));
    }

    // 5. [MASTER 전용] 매니저 삭제
    @Operation(summary = "매니저 권한 삭제", description = "[MASTER] 관리자가 매니저의 권한을 삭제합니다.")
    @DeleteMapping("/managers/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteManager(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthUser loginUser
    ) {
        // 본인 계정 삭제 시도 차단
        if (userId.equals(loginUser.userId())) {
            throw new CustomException(ErrorCode.SELF_RESOURCE_ACCESS_DENIED);
        }
        userAdminService.deleteManager(userId);
        return ResponseEntity.noContent().build();
    }

}