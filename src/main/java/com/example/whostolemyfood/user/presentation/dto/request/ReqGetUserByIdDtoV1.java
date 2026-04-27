package com.example.whostolemyfood.user.presentation.dto.request;

import java.util.UUID;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import com.example.whostolemyfood.user.domain.entity.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqGetUserByIdDtoV1 {

    private UUID userId;
    private String email;
    private String userName;
    private UserRole role;
//    private UserStatus status; // 새로 추가한 상태값도 응답에 포함!

}