package com.noasuhive.supply_chain_management.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class RegistrationVerificationDto {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    // Getters and setters
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
