package com.noasuhive.supply_chain_management.service.auth;

import com.noasuhive.supply_chain_management.dto.auth.ForgotPasswordRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.JwtResponseDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginOtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpVerificationDto;
import com.noasuhive.supply_chain_management.dto.auth.ResetPasswordRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto dto);
    String verifyRegistrationOtpWithToken(String identifier, String otpCode);
    boolean verifyRegistrationOtpSimple(String identifier, String otpCode);
    void registerAfterEmailVerification(UserRegistrationDto dto);
    void registerWithVerificationToken(UserRegistrationDto dto, String verificationToken);
    JwtResponseDto authenticate(LoginRequestDto dto);
    
    // OTP methods
    void sendRegistrationOtp(OtpRequestDto request);
    void verifyRegistrationOtp(OtpVerificationDto dto);
    void verifyRegistrationOtpStep(String identifier, String otpCode);
    void sendLoginOtp(LoginOtpRequestDto request);
    JwtResponseDto authenticateWithOtp(OtpVerificationDto dto);
    
    // Forgot password methods
    void sendForgotPasswordOtp(ForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
}

