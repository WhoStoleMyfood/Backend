package com.example.whostolemyfood.user.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReqManagerCreateDtoV1 {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 4, max = 10, message = "이름은 4자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 영문만 가능합니다.")
    private String name;

    @NotBlank(message = "관리자 인증 토큰은 필수입니다.")
    private String adminToken;
}