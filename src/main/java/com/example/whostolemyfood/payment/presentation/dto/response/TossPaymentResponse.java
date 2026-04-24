package com.example.whostolemyfood.payment.presentation.dto.response;

public record TossPaymentResponse (
    String paymentKey,
    String orderId,
    String status,         // DONE, WAITING_FOR_DEPOSIT, CANCELED 등
    String method,         // 카드, 가상계좌, 간편결제, 계좌이체 등
    Long totalAmount,
    String approvedAt,
    String requestedAt,
    String orderName,

    // 결제 수단별 상세 (해당하지 않으면 null)
    CardInfo card,
    VirtualAccountInfo virtualAccount,
    EasyPayInfo easyPay,
    Failure failure,
    Receipt receipt
) {
    public record CardInfo(
        String issuerCode, String number, String cardType, String ownerType, Long amount
) {}

    public record VirtualAccountInfo(
            String accountNumber, // 가상계좌 번호
            String bankCode,      // 은행 코드 (20=우리 등)
            String customerName,  // 입금자명
            String dueDate,       // 입금 기한
            String settlementStatus // 정산 상태
    ) {}

    public record EasyPayInfo(
            String provider, Long amount, Long discountAmount
    ) {}

    public record Failure(
            String code, String message
    ) {}

    public record Receipt(
            String url // 영수증 확인 주소
    ) {}

}
