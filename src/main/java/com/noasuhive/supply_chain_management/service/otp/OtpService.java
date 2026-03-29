package com.noasuhive.supply_chain_management.service.otp;

import com.noasuhive.supply_chain_management.dto.auth.OtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpVerificationDto;

public interface OtpService {
    
    // Send OTP for registration (both email and phone)
    void sendRegistrationOtp(OtpRequestDto request);
    
    // Send OTP for login (email or phone)
    void sendLoginOtp(String identifier);
    
    // Verify OTP
    boolean verifyOtp(OtpVerificationDto dto);
    
    // Generate OTP
    String generateOtp();
    
    // Cleanup expired OTPs
    void cleanupExpiredOtps();
}
