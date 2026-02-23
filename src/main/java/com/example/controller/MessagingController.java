package com.example.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.request.ApiResponse;
import com.example.dto.request.MessageRequest;
import com.example.messaging.RedisMessagePublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class MessagingController {

    private final RedisMessagePublisher messagePublisher;

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<String>> publishMessage(@RequestBody MessageRequest request) {
        log.info("Publishing message: {}", request);
        messagePublisher.publish(request);
        
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .code(200)
                .message("Message published successfully")
                .result("Message sent to Redis")
                .build()
        );
    }

    @PostMapping("/publish-text")
    public ResponseEntity<ApiResponse<String>> publishTextMessage(@RequestBody String message) {
        log.info("Publishing text: {}", message);
        messagePublisher.publish(message);
        
        return ResponseEntity.ok(
            ApiResponse.<String>builder()
                .code(200)
                .message("Text published successfully")
                .result(message)
                .build()
        );
    }
}
