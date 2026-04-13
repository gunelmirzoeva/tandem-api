package com.example.tandem_api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class OtpGeneratorTest {

    @InjectMocks
    private OtpGenerator otpGenerator;

    @Test
    void shouldGenerateSixDigitOtp() {
        String otp = otpGenerator.generate();
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d+"));
    }

    @Test
    void shouldGenerateOtpBetween100000And999999() {
        int otp = Integer.parseInt(otpGenerator.generate());
        assertTrue(otp >= 100000 && otp <= 999999);
    }
}
