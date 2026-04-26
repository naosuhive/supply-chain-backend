package com.noasuhive.supply_chain_management.dto.auth;

public class VerificationResponseDto {
    private String message;
    private String verificationToken;
    private String identifier;

    public VerificationResponseDto(String message, String verificationToken, String identifier) {
        this.message = message;
        this.verificationToken = verificationToken;
        this.identifier = identifier;
    }

    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
}
