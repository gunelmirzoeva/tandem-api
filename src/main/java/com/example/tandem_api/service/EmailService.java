package com.example.tandem_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async("taskExecutor")
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Account Verification");
        mail.setText("Your account verification code: " + otp + "\n\nThis code expires in 10 minutes");
        javaMailSender.send(mail);
    }
}
