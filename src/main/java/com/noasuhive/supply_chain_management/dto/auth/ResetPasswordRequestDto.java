package com.noasuhive.supply_chain_management.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequestDto {
    
    @NotBlank(message = "Identifier is required")
    private String identifier; // Email or phone number
    
    @NotBlank(message = "OTP is required")
    private String otpCode;
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;
    
    public ResetPasswordRequestDto() {}
    
    public ResetPasswordRequestDto(String identifier, String otpCode, String newPassword) {
        this.identifier = identifier;
        this.otpCode = otpCode;
        this.newPassword = newPassword;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getOtpCode() {
        return otpCode;
    }
    
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
