package com.example.tandem_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpService otpService;

    private final UUID userId = UUID.randomUUID();



    @Test
    void saveOtp_shouldStoreOtpWithTenMinuteTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpService.saveOtp(userId, "123456");
        verify(valueOperations).set("otp:" + userId, "123456", 10, TimeUnit.MINUTES);
    }

    @Test
    void getOtp_shouldReturnOtp_whenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + userId)).thenReturn("123456");

        Optional<String> result = otpService.getOtp(userId);
        assertTrue(result.isPresent());
        assertEquals("123456", result.get());
    }

    @Test
    void getOtp_shouldReturnEmpty_whenKeyDoesNotExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + userId)).thenReturn(null);
        Optional<String> result = otpService.getOtp(userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteOtp_shouldDeleteAllRelatedKeys() {
        otpService.deleteOtp(userId);
        verify(redisTemplate).delete("otp:" + userId);
        verify(redisTemplate).delete("otp:resend:" + userId);
        verify(redisTemplate).delete("otp:cooldown:" + userId);
        verify(redisTemplate).delete("otp:attempts:" + userId);
    }

    @Test
    void invalidateOtp_shouldDeleteOnlyOtpKey() {
        otpService.invalidateOtp(userId);
        verify(redisTemplate).delete("otp:" + userId);
        verify(redisTemplate, never()).delete("otp:attempts:" + userId);
    }

    @Test
    void incrementAttempts_shouldSetTtlOnFirstIncrement() {
        String key = "otp:attempts:" + userId;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L);

        int result = otpService.incrementAttempts(userId);

        assertEquals(1, result);
        verify(redisTemplate).expire(key, 10, TimeUnit.MINUTES);
    }

    @Test
    void incrementAttempts_shouldNotSetTtlAfterFirstIncrement() {
        String key = "otp:attempts:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(3L);

        int result = otpService.incrementAttempts(userId);

        assertEquals(3, result);
        verify(redisTemplate, never()).expire(any(), anyLong(), any());
    }

    @Test
    void incrementAttempts_shouldReturnZero_whenIncrementReturnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("otp:attempts:" + userId)).thenReturn(null);

        int result = otpService.incrementAttempts(userId);
        assertEquals(0, result);
    }

    @Test
    void getAttempts_shouldReturnCount_whenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:attempts:" + userId)).thenReturn("3");

        assertEquals(3, otpService.getAttempts(userId));
    }

    @Test
    void getAttempts_shouldReturnZero_whenKeyDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:attempts:" + userId)).thenReturn(null);

        assertEquals(0, otpService.getAttempts(userId));
    }


    @Test
    void saveResendCount_shouldIncrementAndResetTtl() {
        String key = "otp:resend:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L);

        otpService.saveResendCount(userId);

        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(key, 10, TimeUnit.MINUTES);
    }

    @Test
    void getResendCount_shouldReturnCount_whenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:resend:" + userId)).thenReturn("2");

        assertEquals(2, otpService.getResendCount(userId));
    }

    @Test
    void getResendCount_shouldReturnZero_whenKeyDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:resend:" + userId)).thenReturn(null);

        assertEquals(0, otpService.getResendCount(userId));
    }

    @Test
    void setCooldown_shouldStoreKeyWithTwoMinuteTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpService.setCooldown(userId);
        verify(valueOperations).set("otp:cooldown:" + userId, "1", 2, TimeUnit.MINUTES);
    }

    @Test
    void isCooldownActive_shouldReturnTrue_whenKeyExists() {
        when(redisTemplate.hasKey("otp:cooldown:" + userId)).thenReturn(true);
        assertTrue(otpService.isCooldownActive(userId));
    }

    @Test
    void isCooldownActive_shouldReturnFalse_whenKeyDoesNotExist() {
        when(redisTemplate.hasKey("otp:cooldown:" + userId)).thenReturn(false);
        assertFalse(otpService.isCooldownActive(userId));
    }
}

