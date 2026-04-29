package com.example.whostolemyfood.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.whostolemyfood.user.domain.entity.UserRole;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE) // Jackson을 위한 기본 생성자
@AllArgsConstructor // 전체 생성자
public class ReqSignUpDtoV1 {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:\";'<>?,./]).{8,}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 최소 1자 이상 포함하고 8자 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 4, max = 10, message = "이름은 4자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 영문만 가능합니다.")
    private String userName;

    @NotNull(message = "유저 타입은 필수입니다.")
    private UserRole userRole;

    //private String address;

    // 관리자 가입을 위한 토큰
    private String adminToken;

}
