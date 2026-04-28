package com.example.whostolemyfood.user.presentation.dto.response;

import com.example.whostolemyfood.user.domain.entity.UserEntity;
import com.example.whostolemyfood.user.domain.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResGetUserByIdDtoV1 {
    private String email;
    private String name;
    private UserRole role;

    public ResGetUserByIdDtoV1(UserEntity user) {
        this.email = user.getUserEmail();
        this.name = user.getUserName();
        this.role = user.getUserRole();
    }
}