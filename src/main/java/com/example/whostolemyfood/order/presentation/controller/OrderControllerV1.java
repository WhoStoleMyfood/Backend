package com.example.whostolemyfood.order.presentation.controller;

import com.example.whostolemyfood.global.response.PageResponse;
import com.example.whostolemyfood.global.util.PageUtil;
import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderRequestDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderStatusDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order API", description = "주문 관리 API")
@RestController
@RequestMapping("/api/v1/orders") // [SA 준수] v1 경로 추가
@RequiredArgsConstructor
@Validated
public class OrderControllerV1 {

    private final OrderServiceV1 orderService;

    /**
     * 주문 생성 API
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성하고 상세 내역을 반환합니다.")
    @PostMapping
    public ResponseEntity<ResCreateOrderDtoV1> createOrder(@Valid @RequestBody ReqCreateOrderDtoV1 request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    /**
     * 주문 상세 조회 API
     */
    @Operation(summary = "주문 상세 조회", description = "주문 ID를 통해 특정 주문의 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<ResGetOrderDtoV1> getOrder(@PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    /**
     * 주문 목록 조회 API (페이징 및 검색)
     */
    @Operation(summary = "주문 목록 조회 및 검색", description = "가게 ID, 숨김 여부 등을 필터로 주문 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponse<ResGetOrderListDtoV1>> getOrders(
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @RequestParam(name = "isHidden", required = false) Boolean isHidden,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // PageUtil을 사용하여 페이지 사이즈 제한 로직 적용
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);
        Page<ResGetOrderListDtoV1> orders = orderService.getOrders(storeId, isHidden, validatedPageable);
        
        // PageResponse를 사용하여 규격화된 페이징 응답 반환
        return ResponseEntity.ok(new PageResponse<>(orders));
    }

    /**
     * 주문 수정(요청사항 수정) API
     */
    @Operation(summary = "주문 요청사항 수정", description = "주문 수락 전(PENDING) 상태에서 고객의 요청사항을 수정합니다.")
    @PutMapping("/{orderId}")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderRequest(
            @PathVariable("orderId") UUID orderId, 
            @Valid @RequestBody ReqUpdateOrderRequestDtoV1 requestDto) {
        return ResponseEntity.ok(orderService.updateOrderRequest(orderId, requestDto.getRequest()));
    }

    /**
     * 주문 취소 API
     */
    @Operation(summary = "주문 취소", description = "생성 후 5분 이내의 주문을 취소 상태(CANCELLED)로 변경합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ResGetOrderDtoV1> cancelOrder(@PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    /**
     * 주문 상태 변경 API (사장님/관리자용)
     */
    @Operation(summary = "주문 상태 변경", description = "사장님 또는 관리자가 주문의 진행 상태를 단계별로 변경합니다.")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderStatus(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody ReqUpdateOrderStatusDtoV1 request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    /**
     * 주문 삭제 API
     */
    @Operation(summary = "주문 삭제", description = "주문 내역을 Soft Delete 처리합니다. 배달 완료된 주문은 삭제 불가합니다.")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("orderId") UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
