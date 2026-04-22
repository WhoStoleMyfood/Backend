package com.example.whostolemyfood.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.example.whostolemyfood.user.domain.entity.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReqSignUpDtoV1 {


    @NotNull(message = "유저 타입은 필수입니다.")
    private final UserRole userRole;

    @NotBlank(message = "필수 입력값을 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    private final String email;

    /*대소문자 포함 영문 + 숫자 + 특수문자를 최소 1글자씩 포함합니다.
    비밀번호는 최소 8글자 이상이어야 합니다.*/
    @NotBlank(message = "필수 입력값을 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:\";'<>?,./]).{8,}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 최소 1자 이상 포함하고 8자 이상이어야 합니다."
    )
	/*^ // 문자열의 시작
	(?=.*[a-z])      // 문자열 어딘가 소문자가 최소 1개
	(?=.*[A-Z])       // 문자열 어딘가 대문자가 최소 1개
	(?=.*\d)          // 문자열 어딘가 숫자가 최소 1개 (\d는 digit = 숫자)
	(?=.*[!@#$%^&*()_+\-={}:\";'<>?,./]) // 특수문자가 최소 1개
	.{8,}			// 전체 길이는 최소 8자 이상
	$ // 문자열 끝
	*/
    private final String password;

    private final String userName;
}
