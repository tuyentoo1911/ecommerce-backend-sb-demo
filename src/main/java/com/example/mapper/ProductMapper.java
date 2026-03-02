package com.example.mapper;

import org.mapstruct.Mapper;
import com.example.dto.request.ProductRequest;
import com.example.dto.response.ProductResponse;
import com.example.javaspringboot.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest request);
    ProductResponse toResponse(Product product);
}
