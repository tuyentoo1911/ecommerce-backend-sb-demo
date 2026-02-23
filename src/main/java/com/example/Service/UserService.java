package com.example.Service;

import org.springframework.stereotype.Service;
import com.example.javaspringboot.repository.UserRepository;
import com.example.javaspringboot.entity.User;
import com.example.dto.request.UserCreationRequest;
import java.util.List;
import java.util.UUID;
import com.example.dto.request.UserUpdateRequest;
import com.example.exception.Errorcode;
import com.example.exception.AppException;
import com.example.mapper.UserMapper;
import com.example.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import com.example.enums.Role;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper usermapper;
    PasswordEncoder passwordEncoder;

    public User createRequest(UserCreationRequest request) {
        log.info("Creating user: {}", request.getUsername());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(Errorcode.USER_EXISTS);
        }
        User user = usermapper.toUser(request);
        user.setId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream().map(usermapper::toUserResponse).toList();

    }
    @PostAuthorize("returnObject.id == authentication.name")
    public UserResponse getUser(String userid) {
        log.info("In method getUser by id");
        return usermapper.toUserResponse(userRepository.findById(userid).orElseThrow(() -> new RuntimeException("User not found")));

    }

    public UserResponse updateUser(String userid, UserUpdateRequest request) {
        User user = userRepository.findById(userid).orElseThrow(() -> new RuntimeException("User not found"));
        usermapper.updateUser(user, request);
        return usermapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userid) {
        User user = userRepository.findById(userid).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public UserResponse getCurrentUser() {
        var SecurityContext = SecurityContextHolder.getContext();
        String username = SecurityContext.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return usermapper.toUserResponse(user);
    }
}
