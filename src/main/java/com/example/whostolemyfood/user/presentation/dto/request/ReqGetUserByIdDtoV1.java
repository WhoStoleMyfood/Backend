package com.example.whostolemyfood.user.presentation.dto.request;

import java.util.UUID;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.entity.UserStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Schema(description = "유저 조회 요청 객체")
@NoArgsConstructor
@AllArgsConstructor
public class ReqGetUserByIdDtoV1 {

    @Schema(description = "유저아이디")
    private UUID userId;
    @Schema(description = "유저 이메일", example = "test@email.com")
    private String email;
    @Schema(description = "유저 비밀번호", example = "1234Asdf!")
    private String userName;
    @Schema(description = "유저 권한", example = "OWNER")
    private UserRole role;
//    private UserStatus status; // 새로 추가한 상태값도 응답에 포함!

}