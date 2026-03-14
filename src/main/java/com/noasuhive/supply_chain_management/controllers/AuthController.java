package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.auth.JwtResponseDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.UserRegistrationDto;
import com.noasuhive.supply_chain_management.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        JwtResponseDto resp = authService.authenticate(dto);
        return ResponseEntity.ok(resp);
    }
}

