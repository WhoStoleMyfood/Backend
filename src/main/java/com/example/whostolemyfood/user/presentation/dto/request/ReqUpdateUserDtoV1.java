package com.example.whostolemyfood.user.presentation.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReqUpdateUserDtoV1 {
    private String name;
    private String password;
    private String address;
}
