package com.example.controller;

import org.springframework.web.bind.annotation.RestController;
import com.example.Service.UserService;
import com.example.dto.request.UserCreationRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.javaspringboot.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import com.example.dto.request.UserUpdateRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import com.example.dto.request.ApiResponse;
import com.example.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.Builder;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Creating user: {}", request.getUsername());
        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("User created successfully");
        apiResponse.setResult(userService.createRequest(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication.getName());
        authentication.getAuthorities().forEach(authority -> {
        log.info("Authority: {}", authority.getAuthority());
});        
        return ApiResponse.<List<UserResponse>>builder().result(userService.getAllUsers()).build();
    }

    @GetMapping("/{userid}")
    ApiResponse<UserResponse> getUser(@PathVariable("userid") @Valid String userid) {
        return ApiResponse.<UserResponse>builder().result(userService.getUser(userid)).build();
    }
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().result(userService.getCurrentUser()).build();
    }
    @PutMapping("/{userid}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userid") @Valid String userid, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.updateUser(userid, request)).build();
    }

    @DeleteMapping("/{userid}")
    ApiResponse<Void> deleteUser(@PathVariable("userid") @Valid String userid) {
        userService.deleteUser(userid);
        return ApiResponse.<Void>builder().result(null).build();
    }
}
