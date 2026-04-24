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
    ORDER_NOT_OWNER(HttpStatus.FORBIDDEN, "O008", "본인의 주문 정보만 접근할 수 있습니다."),
    ORDER_FORBIDDEN_FOR_OWNER(HttpStatus.FORBIDDEN, "O009", "해당 주문이 속한 가게의 사장님만 상태 변경이 가능합니다."),

    // Domain Validation (Order Creation)
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "해당 가게를 찾을 수 없습니다."),
    STORE_CLOSED(HttpStatus.BAD_REQUEST, "D002", "현재 영업 중인 가게가 아닙니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "D003", "가게에 등록된 메뉴를 찾을 수 없습니다."),
    PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "D004", "메뉴 가격이 변동되었습니다. 다시 확인해 주세요."),
    ORDER_MIN_PRICE_NOT_MET(HttpStatus.BAD_REQUEST, "O010", "최소 주문 금액을 만족하지 않습니다."),

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}