package com.example.controller;

import org.springframework.web.bind.annotation.*;
import com.example.Service.ProductService;
import com.example.dto.request.ProductRequest;
import com.example.dto.request.ApiResponse;
import com.example.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @GetMapping
    ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProducts())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductResponse> getProduct(@PathVariable String id) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProduct(id))
                .build();
    }

    @PostMapping
    ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ProductResponse> updateProduct(
            @PathVariable String id,
            @RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.<Void>builder().build();
    }
}
