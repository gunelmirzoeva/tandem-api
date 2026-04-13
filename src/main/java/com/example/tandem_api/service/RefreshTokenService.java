package com.example.tandem_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_TTL_DAYS = 7;
    private static final String REFRESH_KEY = "refresh:%s:%s";
    private static final String ROTATED_KEY = "refresh:rotated:%s";
    private final RedisTemplate<String, String> redisTemplate;

    public void store(UUID userId, String tokenId) {
        String key = String.format(REFRESH_KEY, userId, tokenId);
        redisTemplate.opsForValue().set(key, tokenId, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
    }

    public boolean exists(UUID userId, String tokenId) {
        String key = String.format(REFRESH_KEY, userId, tokenId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(UUID userId, String tokenId) {
        String key = String.format(REFRESH_KEY, userId, tokenId);
        redisTemplate.delete(key);
    }

    public void deleteAll(UUID userId) {
        String pattern = String.format("refresh:%s:*", userId);
        Set<String> keys = redisTemplate.keys(pattern);
        if(keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void markAsRotated(String tokenId) {
        String key = String.format(ROTATED_KEY, tokenId);

        redisTemplate.opsForValue().set(key, "1", REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
    }

    public boolean isRotated(String tokenId) {
        String key = String.format(ROTATED_KEY, tokenId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void rotate(UUID userId, String oldTokenId, String newTokenId) {
        markAsRotated(oldTokenId);
        delete(userId, oldTokenId);
        store(userId, newTokenId);
    }

}
