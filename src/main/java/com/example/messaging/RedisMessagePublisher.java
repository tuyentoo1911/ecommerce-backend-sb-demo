package com.example.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;

    public void publish(String message) {
        log.info("Publishing message: {}", message);
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }

    public void publish(Object object) {
        log.info("Publishing object: {}", object);
        redisTemplate.convertAndSend(topic.getTopic(), object);
    }
}
