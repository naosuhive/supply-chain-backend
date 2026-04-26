package com.noasuhive.supply_chain_management.service.otp;

import com.noasuhive.supply_chain_management.dto.auth.OtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpVerificationDto;

public interface OtpService {
    
    // Send OTP for registration (both email and phone)
    void sendRegistrationOtp(OtpRequestDto request);
    
    // Send OTP for login (email or phone)
    void sendLoginOtp(String identifier);
    
    // Verify OTP (marks as used)
    boolean verifyOtp(OtpVerificationDto dto);
    
    // Verify OTP for registration (doesn't mark as used immediately)
    boolean verifyRegistrationOtp(String identifier, String otpCode);
    
    // Verify OTP for registration and return verification token
    String verifyRegistrationOtpWithToken(String identifier, String otpCode);
    
    // Verify OTP for registration (simple - just mark as verified)
    boolean verifyRegistrationOtpSimple(String identifier, String otpCode);
    
    // Check if email is verified for registration
    boolean isEmailVerified(String identifier);
    
    // Mark OTP as used after registration is complete
    void markOtpAsUsed(String identifier, String otpCode);
    
    // Complete registration using verification token
    boolean validateVerificationToken(String verificationToken, String identifier);
    
    // Generate OTP
    String generateOtp();
    
    // Cleanup expired OTPs
    void cleanupExpiredOtps();
}
