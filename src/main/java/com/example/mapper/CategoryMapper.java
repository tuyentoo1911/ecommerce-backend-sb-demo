package com.example.mapper;

import com.example.dto.response.CategoryResponse;
import com.example.javaspringboot.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
