package com.example.controller;

import com.example.Service.CategoryService;
import com.example.dto.request.ApiResponse;
import com.example.dto.request.CategoryRequest;
import com.example.dto.response.CategoryResponse;
import com.example.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> getCategory(@PathVariable String id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategory(id))
                .build();
    }

    @GetMapping("/{id}/products")
    ApiResponse<List<ProductResponse>> getProductsByCategory(@PathVariable String id) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(categoryService.getProductsByCategoryId(id))
                .build();
    }

    @PostMapping
    ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder().build();
    }
}
