package com.example.whostolemyfood.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.whostolemyfood.user.domain.entity.UserRole;

import lombok.*;

@Getter
@Builder
@Schema(description = "사용자 계정 생성 요청 객체")
@NoArgsConstructor(access = AccessLevel.PRIVATE) // Jackson을 위한 기본 생성자
@AllArgsConstructor // 전체 생성자
public class ReqSignUpDtoV1 {

    @Schema(description = "이메일", example = "test@email.com")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "1234Asdf!")
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:\";'<>?,./]).{8,15}$",
            message = "비밀번호는 대소문자, 숫자, 특수문자를 포함하여 8~15자로 입력해주세요."
    )
    private String password;

    @Schema(description = "사용자 이름", example = "오너테스터")
    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 4, max = 10, message = "이름은 4자 이상 10자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "이름은 알파벳 소문자와 숫자만 사용 가능합니다.")
    private String userName;

    @Schema(description = "유저 권한", example = "OWNER")
    @NotNull(message = "유저 타입은 필수입니다.")
    private UserRole userRole;

    //private String address;

    // 관리자 가입을 위한 토큰
    @Schema(description = "관리자 가입 토큰")
    private String adminToken;

}
