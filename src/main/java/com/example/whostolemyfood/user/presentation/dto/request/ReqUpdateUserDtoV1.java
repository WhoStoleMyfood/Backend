package com.example.whostolemyfood.user.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReqUpdateUserDtoV1 {
    private String userName;
    private String password;
}
