package com.example.whostolemyfood.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReqLoginDtoV1 {
    @NotBlank(message = "필수 입력값을 입력해주세요.")
    private final String email;

    @NotBlank(message = "필수 입력값을 입력해주세요.")
    private final String password;
}
