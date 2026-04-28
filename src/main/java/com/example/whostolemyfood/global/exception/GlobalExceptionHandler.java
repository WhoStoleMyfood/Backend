package com.example.whostolemyfood.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 유효성 검사 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .value(String.valueOf(error.getRejectedValue()))
                        .reason(error.getDefaultMessage())
                        .build())
                .toList();

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .code("VALIDATION_ERROR")
                .message("입력값이 올바르지 않습니다.")
                .errors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 존재하지 않는 데이터 요청이나 잘못된 인자 값 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .code("BUSINESS_ERROR")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 그 외 비즈니스 로직 오류 처리 (주문 상태 위반 등)
     */
    @ExceptionHandler(IllegalStateException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .code("BUSINESS_ERROR")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }
    /**
     * (추가) 도메인별 상세 에러 처리를 위한 공통 핸들러
     * 제가 작업하면서 상세 에러 코드가 필요해서 추가해 뒀어요!
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * 접근 권한이 없을 때 발생하는 예외 처리 (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * 잘못된 형식의 JSON 데이터나 타입 불일치 발생 시 처리 (예: 잘못된 Enum 값)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.getCode())
                        .message("입력 형식이 잘못되었습니다. (예: 존재하지 않는 주문 상태값)")
                        .build());
    }

}