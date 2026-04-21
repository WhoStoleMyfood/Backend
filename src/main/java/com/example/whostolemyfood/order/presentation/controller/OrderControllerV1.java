package com.example.whostolemyfood.order.presentation.controller;

import com.example.whostolemyfood.order.application.service.OrderServiceV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderRequestDtoV1;
import com.example.whostolemyfood.order.presentation.dto.request.ReqUpdateOrderStatusDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResCreateOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.whostolemyfood.order.presentation.dto.response.ResGetOrderListDtoV1;
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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderControllerV1 {

    private final OrderServiceV1 orderService;

    /**
     * 주문 생성 API
     */
    @PostMapping
    public ResponseEntity<ResCreateOrderDtoV1> createOrder(@Valid @RequestBody ReqCreateOrderDtoV1 request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    /**
     * 주문 상세 조회 API
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ResGetOrderDtoV1> getOrder(@PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    /**
     * 주문 목록 조회 API (페이징 및 검색)
     */
    @GetMapping
    public ResponseEntity<Page<ResGetOrderListDtoV1>> getOrders(
            @RequestParam(name = "storeId", required = false) UUID storeId,
            @RequestParam(name = "isHidden", required = false) Boolean isHidden,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            pageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }
        
        return ResponseEntity.ok(orderService.getOrders(storeId, isHidden, pageable));
    }

    /**
     * 주문 수정(요청사항 수정) API
     */
    @PutMapping("/{orderId}")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderRequest(
            @PathVariable("orderId") UUID orderId, 
            @Valid @RequestBody ReqUpdateOrderRequestDtoV1 requestDto) {
        return ResponseEntity.ok(orderService.updateOrderRequest(orderId, requestDto.getRequest()));
    }

    /**
     * 주문 취소 API
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ResGetOrderDtoV1> cancelOrder(@PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    /**
     * 주문 상태 변경 API (사장님/관리자용)
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ResGetOrderDtoV1> updateOrderStatus(
            @PathVariable("orderId") UUID orderId,
            @Valid @RequestBody ReqUpdateOrderStatusDtoV1 request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    /**
     * 주문 삭제 API
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("orderId") UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
