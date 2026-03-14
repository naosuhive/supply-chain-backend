package com.noasuhive.supply_chain_management.dto.auth;

public class JwtResponseDto {
    private String token;
    private String tokenType = "Bearer";

    public JwtResponseDto() {}
    public JwtResponseDto(String token) { this.token = token; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}

