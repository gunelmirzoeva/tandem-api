package com.example.tandem_api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;


    @Test
    void sendOtpEmail_shouldSendEmailWithCorrectFields() {
        emailService.sendOtpEmail("gunel@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"gunel@example.com"}, sent.getTo());
        assertEquals("Account Verification", sent.getSubject());
        assert sent.getText() != null;
        assertTrue(sent.getText().contains("123456"));
        assertTrue(sent.getText().contains("10 minutes"));
    }

    @Test
    void sendOtpEmail_shouldCallMailSenderOnce() {
        emailService.sendOtpEmail("gunel@example.com", "123456");
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
