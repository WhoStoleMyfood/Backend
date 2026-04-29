package com.example.whostolemyfood.user.application.security;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

//토큰 저장용 객체
@Getter
@AllArgsConstructor
public class TokenResult {
    private final UUID userId;
    private final String accessToken;
    private final String refreshToken;
}