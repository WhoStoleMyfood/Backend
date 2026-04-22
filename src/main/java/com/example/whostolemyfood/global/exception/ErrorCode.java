package com.example.whostolemyfood.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_DUPLICATION_EMAIL(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다."),
    USER_WRONG_PW(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 일치하지 않습니다."),

    //Auth
    ACCESS_DENIED(HttpStatus.NOT_FOUND, "A001", "잘못된 권한입니다."),

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}