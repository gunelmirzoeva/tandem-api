package com.example.tandem_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveOtp(UUID userId, String otp){
        redisTemplate.opsForValue().set("otp:" + userId, otp, 10, TimeUnit.MINUTES);
    }

    public Optional<String> getOtp(UUID userId) {
        String otp = redisTemplate.opsForValue().get("otp:" + userId);
        return Optional.ofNullable(otp);
    }

    public void deleteOtp(UUID userId) {
        redisTemplate.delete("otp:" + userId);
        redisTemplate.delete("otp:resend:" + userId);
        redisTemplate.delete("otp:cooldown:" + userId);
        redisTemplate.delete("otp:attempts:" + userId);
    }

    public void invalidateOtp(UUID userId) {
        redisTemplate.delete("otp:" + userId);
    }

    public int incrementAttempts(UUID userId) {
        String key = "otp:attempts:" + userId;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
        return attempts != null ? attempts.intValue() : 0;
    }

    public int getAttempts(UUID userId) {
        String val = redisTemplate.opsForValue().get("otp:attempts:" + userId);
        return val != null ? Integer.parseInt(val) : 0;
    }


    public void saveResendCount(UUID userId) {
        String key = "otp:resend:" + userId;
        Long resend = redisTemplate.opsForValue().increment(key);
        if (resend != null && resend == 1L) {
            redisTemplate.expire(key, 10, TimeUnit.MINUTES);
        }
    }

    public int getResendCount(UUID userId) {
        String val = redisTemplate.opsForValue().get("otp:resend:" + userId);
        return val != null ? Integer.parseInt(val) : 0;
    }

    public void setCooldown(UUID userId) {
        redisTemplate.opsForValue().set("otp:cooldown:" + userId, "1", 2, TimeUnit.MINUTES);
    }

    public boolean isCooldownActive(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("otp:cooldown:" + userId));
    }

    public void saveResetOtp(UUID userId, String otp) {
        redisTemplate.opsForValue().set("pwd:reset:otp:" + userId, otp, 15, TimeUnit.MINUTES);
    }

    public Optional<String> getResetOtp(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get("pwd:reset:otp:" + userId));
    }

    public void deleteResetOtp(UUID userId, String email) {
        redisTemplate.delete("pwd:reset:otp:" + userId);
        redisTemplate.delete("pwd:reset:attempts:" + email);
        redisTemplate.delete("pwd:reset:cooldown:" + userId);
    }

    public int incrementResetAttempts(String email) {
        String key = "pwd:reset:attempts:" + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
        return attempts != null ? attempts.intValue() : 0;
    }

    public int getResetAttempts(String email) {
        String val = redisTemplate.opsForValue().get("pwd:reset:attempts:" + email);
        return val != null ? Integer.parseInt(val) : 0;
    }

    public void setResetCooldown(UUID userId) {
        redisTemplate.opsForValue().set("pwd:reset:cooldown:" + userId, "1", 2, TimeUnit.MINUTES);
    }

    public boolean isResetCooldownActive(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("pwd:reset:cooldown:" + userId));
    }
}
