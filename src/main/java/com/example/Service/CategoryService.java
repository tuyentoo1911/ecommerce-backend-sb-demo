package com.example.Service;

import com.example.dto.request.CategoryRequest;
import com.example.dto.response.CategoryResponse;
import com.example.dto.response.ProductResponse;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import com.example.javaspringboot.entity.Category;
import com.example.javaspringboot.repository.CategoryRepository;
import com.example.javaspringboot.repository.ProductRepository;
import com.example.mapper.CategoryMapper;
import com.example.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    CategoryMapper categoryMapper;
    ProductMapper productMapper;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategory(String id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new AppException(Errorcode.CATEGORY_NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(Errorcode.CATEGORY_NOT_FOUND));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(Errorcode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    public List<ProductResponse> getProductsByCategoryId(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(Errorcode.CATEGORY_NOT_FOUND));
        return productRepository.findByCategory(category.getName()).stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
