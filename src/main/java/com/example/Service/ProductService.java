package com.example.Service;

import org.springframework.stereotype.Service;
import com.example.javaspringboot.repository.ProductRepository;
import com.example.javaspringboot.entity.Product;
import com.example.dto.request.ProductRequest;
import com.example.dto.response.ProductResponse;
import com.example.mapper.ProductMapper;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ProductMapper productMapper;

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProduct(String id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new AppException(Errorcode.PRODUCT_NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toProduct(request);
        product.setId(UUID.randomUUID().toString());
        return productMapper.toResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(Errorcode.PRODUCT_NOT_FOUND));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        
        return productMapper.toResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new AppException(Errorcode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }
}
