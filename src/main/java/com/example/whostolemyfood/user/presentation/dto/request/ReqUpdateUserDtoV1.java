package com.example.whostolemyfood.user.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReqUpdateUserDtoV1 {

    @Size(min = 4, max = 10, message = "이름은 4자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z가-힣]+$", message = "이름은 한글 또는 영문만 가능합니다.")
    private String name;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:\";'<>?,./]).{8,}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 최소 1자 이상 포함하고 8자 이상이어야 합니다."
    )
    private String password;

    private String address;
}
