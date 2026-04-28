package com.example.whostolemyfood.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Getter
@Schema(description = "사용자 로그인 요청 객체")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReqLoginDtoV1 {

    @Schema(description = "이메일", example = "test@email.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @Schema(description = "비밀번호", example = "1234Asdf!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}