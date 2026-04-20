package com.example.whostolemyfood.user.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("고객"),
    OWNER("가게 주인"),
    MANAGER("서비스 담당자"),
    MASTER("최종 관리자");

    private final String description;
}