package com.noasuhive.supply_chain_management.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginOtpRequestDto {
    
    @NotBlank(message = "Identifier is required")
    private String identifier; // can be username, email, or phone number
    
    public LoginOtpRequestDto() {}
    
    public LoginOtpRequestDto(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
