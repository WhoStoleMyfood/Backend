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

    // Auth
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A001", "잘못된 권한입니다."),

    // Address
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "AD001", "존재하지 않는 배송지입니다."),
    ADDRESS_NOT_OWNER(HttpStatus.FORBIDDEN, "AD002", "본인의 배송지만 관리할 수 있습니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없거나 이미 삭제되었습니다."),
    ORDER_CANCEL_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "O002", "주문 생성 후 5분이 경과하여 취소할 수 없습니다."),
    ORDER_CANCEL_NOT_PENDING(HttpStatus.BAD_REQUEST, "O003", "이미 수락된 주문은 취소할 수 없습니다. 가게에 문의해 주세요."),
    ORDER_ALREADY_FINALIZED(HttpStatus.BAD_REQUEST, "O004", "이미 최종 상태(취소/완료)에 도달한 주문은 상태를 변경할 수 없습니다."),
    ORDER_CANNOT_DELETE_DELIVERED(HttpStatus.BAD_REQUEST, "O005", "배달이 완료된 주문은 삭제할 수 없습니다."),
    ORDER_REQUEST_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "O006", "주문이 이미 수락되어 요청사항을 수정할 수 없습니다."),
    ORDER_STATUS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, "O007", "허용되지 않은 주문 상태 변경입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001" ,"해당 스토어를 찾을 수 없습니다"),
    STORE_DUPLICATION_NAME(HttpStatus.BAD_REQUEST, "S002", "이미 존재하는 스토어 이름입니다"),
    STORE_NOT_OWNER(HttpStatus.BAD_REQUEST,"S003", "해당 스토어의 소유주가 아닙니다"),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 메뉴를 찾을 수 없습니다"),
    MENU_DUPLICATION_NAME(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 메뉴 이름입니다"),

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류입니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제를 찾을 수 없습니다."),
    FAIL_TO_MODIFY_STATUS(HttpStatus.BAD_REQUEST, "P002", "결제 상태를 변경할 수 없습니다."),
    FAIL_PAY(HttpStatus.BAD_REQUEST, "P003", "결제가 거절되었습니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,"C001","존재하지 않는 카테고리입니다."),
    CATEGORY_DUPLICATION(HttpStatus.BAD_REQUEST,"C002", "이미 존재하는 카테고리입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

}