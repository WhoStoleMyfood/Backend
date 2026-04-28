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
    SELF_RESOURCE_ACCESS_DENIED(HttpStatus.BAD_REQUEST, "A002", "본인의 정보는 이 API로 처리할 수 없습니다."),

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
    ORDER_MIN_PRICE_NOT_MET(HttpStatus.BAD_REQUEST, "O010", "최소 주문 금액을 만족하지 않습니다."),
    PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "O011", "메뉴 가격이 변동되었습니다. 다시 확인해 주세요."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001" ,"해당 스토어를 찾을 수 없습니다."),
    STORE_DUPLICATION_NAME(HttpStatus.BAD_REQUEST, "S002", "이미 존재하는 스토어 이름입니다."),
    STORE_NOT_OWNER(HttpStatus.BAD_REQUEST,"S003", "해당 스토어의 소유주가 아닙니다."),
    STORE_CLOSED(HttpStatus.BAD_REQUEST, "S004", "현재 영업 중인 가게가 아닙니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 메뉴를 찾을 수 없습니다."),
    MENU_DUPLICATION_NAME(HttpStatus.BAD_REQUEST, "M002", "이미 존재하는 메뉴 이름입니다."),
    MENU_AI_PROMPT_NOT_ALLOWED(HttpStatus.BAD_REQUEST,"M003", "AI 미사용시 프롬프트를 입력할 수 없습니다."),
    MENU_DESCRIPTION_DUPLICATE(HttpStatus.BAD_REQUEST,"M004", "직접 입력한 설명과 AI 생성 요청은 동시에 처리할 수 없습니다."),

    // Ai
    AI_PROMPT_REQUIRE(HttpStatus.BAD_REQUEST, "I001","AI 프롬프트는 필수 입력사항입니다."),
    AI_PROMPT_TOO_LONG(HttpStatus.BAD_REQUEST, "I002", "프롬프트 길이는 100자 이내로 작성해주세요."),
    AI_RESPONSE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR,"I003", "AI 응답 결과가 비어 있습니다. 다시 시도해주세요."),


    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "G002", "입력값이 올바르지 않습니다."),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제를 찾을 수 없습니다."),
    FAIL_TO_MODIFY_STATUS(HttpStatus.BAD_REQUEST, "P002", "결제 상태를 변경할 수 없습니다."),
    FAIL_PAY(HttpStatus.BAD_REQUEST, "P003", "결제가 거절되었습니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,"C001","존재하지 않는 카테고리입니다."),
    CATEGORY_DUPLICATION(HttpStatus.BAD_REQUEST,"C002", "이미 존재하는 카테고리입니다."),

    // Area
    AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "AR001", "존재하지 않는 지역입니다."),
    AREA_DUPLICATION(HttpStatus.BAD_REQUEST, "AR002", "이미 존재하는 지역명입니다."),
    AREA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AR003", "지역 관리 권한이 없습니다."),
    AREA_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "AR004", "활성화된 지역이 아닙니다"),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "R002", "이미 리뷰가 작성된 주문입니다."),
    REVIEW_ONLY_CUSTOMER(HttpStatus.FORBIDDEN, "R003", "고객만 리뷰를 작성/수정할 수 있습니다."),
    REVIEW_NOT_OWNER(HttpStatus.FORBIDDEN, "R004", "본인이 작성한 리뷰만 수정/삭제할 수 있습니다."),
    REVIEW_ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "R005", "주문 완료 상태에서만 리뷰 작성이 가능합니다."),

    // Rating
    RATING_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "RS001", "평점 요약 정보를 찾을 수 없습니다."),
    RATING_REFRESH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RS002", "평점 집계 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}