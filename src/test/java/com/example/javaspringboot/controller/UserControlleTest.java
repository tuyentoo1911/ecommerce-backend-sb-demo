package com.example.javaspringboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.web.servlet.MockMvc;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.Service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dto.request.UserCreationRequest;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import com.example.javaspringboot.entity.User;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.test.context.TestPropertySource;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@TestPropertySource(locations = "classpath:test.properties")
public class UserControlleTest {
        @Autowired
        MockMvc mockMvc;
        @MockitoBean
        UserService userService;

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
                                .password("encodedPassword")
                                .dob(dob)
                                .roles(Set.of("USER"))
                                .build();
        }

        @Test
        void createUserTest() throws Exception {
                // GIVEN
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                String userCreationRequestJson = objectMapper.writeValueAsString(userCreationRequest);
                Mockito.when(userService.createRequest(ArgumentMatchers.any(UserCreationRequest.class)))
                                .thenReturn(user);
                // WHEN ,THEN
                mockMvc.perform(MockMvcRequestBuilders.post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userCreationRequestJson))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1000))
                                .andExpect(MockMvcResultMatchers.jsonPath("result.id").value("1"));

        }

        @Test
        void createUserTest_invalidRequest() throws Exception {
                // GIVEN
                UserCreationRequest invalidRequest = UserCreationRequest.builder()
                                .username("ab")
                                .email("test@test.com")
                                .password("12345678")
                                .dob(dob)
                                .build();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                String userCreationRequestJson = objectMapper.writeValueAsString(invalidRequest);

                // WHEN, THEN
                mockMvc.perform(MockMvcRequestBuilders.post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userCreationRequestJson))
                                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(1002))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                                .value("Username must be between 3 and 20 characters"));
        }
}
