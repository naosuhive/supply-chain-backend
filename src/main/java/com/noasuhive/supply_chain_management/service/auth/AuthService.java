package com.noasuhive.supply_chain_management.service.auth;

import com.noasuhive.supply_chain_management.dto.auth.JwtResponseDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto dto);
    JwtResponseDto authenticate(LoginRequestDto dto);
}

