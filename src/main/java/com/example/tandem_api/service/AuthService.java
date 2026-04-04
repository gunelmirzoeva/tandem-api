package com.example.tandem_api.service;

import com.example.tandem_api.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    VerifyEmailResponse verifyEmail(VerifyEmailRequest request);
    ResendOtpResponse resendOtp(ResendOtpRequest request);
}
