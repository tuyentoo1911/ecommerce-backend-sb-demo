package com.example.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisMessageSubscriber {

    public void onMessage(String message) {
        log.info("Received message: {}", message);
        processMessage(message);
    }

    private void processMessage(String message) {
        log.info("Processing: {}", message);
    }
}
