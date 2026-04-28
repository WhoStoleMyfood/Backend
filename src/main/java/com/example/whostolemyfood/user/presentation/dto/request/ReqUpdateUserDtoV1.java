package com.example.whostolemyfood.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "유저 수정 요청 객체")
@NoArgsConstructor
public class ReqUpdateUserDtoV1 {
    private String name;
    private String password;
    private String address;
}
