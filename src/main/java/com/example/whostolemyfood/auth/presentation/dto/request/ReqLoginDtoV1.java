package com.example.whostolemyfood.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReqLoginDtoV1 {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}