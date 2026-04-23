package com.example.whostolemyfood.auth.presentation.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResSignUpDtoV1 {

    private final String email;
    private final String userName;

}
