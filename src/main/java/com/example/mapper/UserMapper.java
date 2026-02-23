package com.example.mapper;

import org.mapstruct.Mapper;
import com.example.dto.request.UserCreationRequest;
import com.example.javaspringboot.entity.User;
import com.example.dto.request.UserUpdateRequest;
import org.mapstruct.MappingTarget;
import com.example.dto.response.UserResponse;
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
