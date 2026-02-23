package com.example.javaspringboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import com.example.Service.UserService;
import com.example.dto.request.UserCreationRequest;
import java.time.LocalDate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.javaspringboot.repository.UserRepository;
import java.util.Set;
import com.example.javaspringboot.entity.User;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import com.example.exception.AppException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.assertj.core.api.Assertions;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class UserServiceTest {
    @Autowired
    UserService userService;

    @MockitoBean
    UserRepository userRepository;
    UserCreationRequest userCreationRequest;
    User user;
    LocalDate dob;

    @BeforeEach
    void initData() {
        dob = LocalDate.of(1990, 1, 1);
        userCreationRequest = UserCreationRequest.builder()
                .username("test")
                .email("test@test.com")
                .password("12345678")
                .dob(dob)
                .build();

        user = User.builder()
                .id("1")
                .username("test")
                .email("test@test.com")
                .firstName("test")
                .lastName("test")
                .dob(dob)
                .roles(Set.of("USER"))
                .build();

        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(user);
    }

    @Test
    void createUserTest() {
        // GIVEN
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createRequest(userCreationRequest));
        // THEN
        Assertions.assertThat(exception.getErrorcode().getCode()).isEqualTo(1001);

    }
}