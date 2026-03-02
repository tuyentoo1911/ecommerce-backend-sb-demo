package com.example.controller;

import org.springframework.web.bind.annotation.*;
import com.example.Service.OrderService;
import com.example.dto.request.ApiResponse;
import com.example.dto.request.CartItemRequest;
import com.example.dto.response.OrderResponse;
import jakarta.validation.Valid;
import com.example.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    /** Order thẳng luôn: mua ngay 1 sản phẩm, không qua giỏ hàng. */
    @PostMapping("/buy-now")
    ApiResponse<OrderResponse> buyNow(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.buyNow(request))
                .build();
    }

    @PostMapping
    ApiResponse<OrderResponse> checkout() {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.checkout())
                .build();
    }

    @GetMapping("/my")
    ApiResponse<List<OrderResponse>> getMyOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getMyOrders())
                .build();
    }

    @GetMapping
    ApiResponse<List<OrderResponse>> getAllOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrders())
                .build();
    }

    @PutMapping("/{id}/cancel")
    ApiResponse<OrderResponse> cancelOrder(@PathVariable String id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.cancelOrder(id))
                .build();
    }

    @PutMapping("/{id}/status")
    ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrderStatus(id, status))
                .build();
    }
}
