package com.example.whostolemyfood.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("활동 중"),
    BANNED("차단됨"),
    DELETED("탈퇴됨");

    private final String description;
}