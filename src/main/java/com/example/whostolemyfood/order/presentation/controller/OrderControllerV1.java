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
import com.example.whostolemyfood.user.application.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order API", description = "주문 관리 API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class OrderControllerV1 {

    private final OrderServiceV1 orderService;

    /**
     * 주문 생성 API (CUSTOMER 전용)
     */
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다. (CUSTOMER 전용)")
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<ResCreateOrderDtoV1> createOrder(
            @Valid @RequestBody ReqCreateOrderDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(orderService.createOrder(request, authUser.userId(), authUser.role()));
    }

    /**
     * 주문 상세 조회 API
     */
    @Operation(summary = "주문 상세 조회", description = "본인의 주문 또는 내 가게 주문, 관리자 권한으로 조회합니다.")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<ResGetOrderDtoV1> getOrder(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(orderService.getOrder(orderId, authUser.userId(), authUser.role()));
    }

    /**
     * 주문 목록 조회 및 검색 API
     */
    @Operation(summary = "주문 목록 조회 및 검색", description = "권한에 따라 접근 가능한 주문 목록을 필터링하여 조회합니다.")
    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<PageResponse<ResGetOrderListDtoV1>> getOrders(
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @RequestParam(name = "isHidden", required = false) Boolean isHidden,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AuthUser authUser) {
        
        Pageable validatedPageable = PageUtil.validatePageSize(pageable);
        Page<ResGetOrderListDtoV1> orders = orderService.getOrders(storeId, isHidden, validatedPageable, authUser.userId(), authUser.role());
        
        return ResponseEntity.ok(new PageResponse<>(orders));
    }

    /**
     * 주문 요청사항 수정 API (CUSTOMER 전용)
     */
    @Operation(summary = "주문 요청사항 수정", description = "본인의 주문 중 수락 전(PENDING) 상태에서만 수정 가능합니다.")
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderRequest(
            @PathVariable("orderId") UUID orderId, 
            @Valid @RequestBody ReqUpdateOrderRequestDtoV1 requestDto,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(orderService.updateOrderRequest(orderId, requestDto.getRequest(), authUser.userId(), authUser.role()));
    }

    /**
     * 주문 취소 API (CUSTOMER, MASTER 전용)
     */
    @Operation(summary = "주문 취소", description = "본인의 주문(5분 이내) 또는 MASTER 권한으로 취소 가능합니다.")
    @PatchMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER')")
    public ResponseEntity<ResGetOrderDtoV1> cancelOrder(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, authUser.userId(), authUser.role()));
    }

    /**
     * 주문 상태 변경 API (OWNER, MANAGER, MASTER 전용)
     */
    @Operation(summary = "주문 상태 변경", description = "가게 사장님(내 가게 한정) 또는 관리자만 상태를 단계별로 변경할 수 있습니다.")
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'MASTER')")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderStatus(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody ReqUpdateOrderStatusDtoV1 request,
            @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus(), authUser.userId(), authUser.role()));
    }

    /**
     * 주문 삭제 API (MASTER 전용 🚨)
     */
    @Operation(summary = "주문 삭제", description = "오직 MASTER 권한으로만 주문 내역을 Soft Delete 처리합니다.")
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal AuthUser authUser) {
        orderService.deleteOrder(orderId, authUser.userId(), authUser.role());
        return ResponseEntity.noContent().build();
    }
}
