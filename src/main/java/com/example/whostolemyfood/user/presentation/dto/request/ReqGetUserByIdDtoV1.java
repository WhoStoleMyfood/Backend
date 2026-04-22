package com.example.whostolemyfood.user.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReqGetUserByIdDtoV1 {
    private UUID userId; // URL 경로변수 대신 바디로 받을 때 사용 가능합니다.
}