package com.noasuhive.supply_chain_management.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequestDto {
    
    @NotBlank(message = "Identifier is required")
    private String identifier; // Email or phone number
    
    public ForgotPasswordRequestDto() {}
    
    public ForgotPasswordRequestDto(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
