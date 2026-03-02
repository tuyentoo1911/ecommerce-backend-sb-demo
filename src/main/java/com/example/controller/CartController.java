package com.example.controller;

import org.springframework.web.bind.annotation.*;
import com.example.Service.CartService;
import com.example.dto.request.CartItemRequest;
import com.example.dto.request.ApiResponse;
import com.example.dto.response.CartItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    ApiResponse<List<CartItemResponse>> getCart() {
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getCart())
                .build();
    }

    @PostMapping("/items")
    ApiResponse<CartItemResponse> addToCart(@RequestBody @Valid CartItemRequest request) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.addToCart(request))
                .build();
    }

    @PutMapping("/items/{id}")
    ApiResponse<CartItemResponse> updateCartItem(
            @PathVariable String id,
            @RequestBody @Valid CartItemRequest request) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.updateCartItem(id, request))
                .build();
    }

    @DeleteMapping("/items/{id}")
    ApiResponse<Void> removeCartItem(@PathVariable String id) {
        cartService.removeCartItem(id);
        return ApiResponse.<Void>builder().build();
    }
}
