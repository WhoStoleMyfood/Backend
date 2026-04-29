package com.example.whostolemyfood.auth.presentation.dto.response;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResLoginDtoV1 {
    private final UUID userId;
    private final String accessToken;
}
