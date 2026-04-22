package com.example.whostolemyfood.user.presentation.dto.response;

import com.example.whostolemyfood.user.domain.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateUserDtoV1 {
    private String email;
    private String name;
    private UserRole role;
    private String message;
}