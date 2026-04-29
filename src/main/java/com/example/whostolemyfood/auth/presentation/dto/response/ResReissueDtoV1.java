package com.example.whostolemyfood.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // 👈 컨트롤러의 new ResReissueDtoV1(result.getAccessToken())을 위해 필수!
public class ResReissueDtoV1 {
    private String accessToken; // 👈 필드명을 accessToken으로 변경
}
