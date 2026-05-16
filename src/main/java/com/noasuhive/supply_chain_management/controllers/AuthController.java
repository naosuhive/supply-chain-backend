package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.auth.ForgotPasswordRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.JwtResponseDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginOtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.LoginRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpVerificationDto;
import com.noasuhive.supply_chain_management.dto.auth.ResetPasswordRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.UserRegistrationDto;
import com.noasuhive.supply_chain_management.dto.auth.RegistrationVerificationDto;
import com.noasuhive.supply_chain_management.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and OTP management APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(summary = "Register new user (after OTP verification)", description = "Step 3: Complete user registration - email must be verified in step 2. For manufacturers: companyName is required. For retailers: businessName is required. Address is optional for all user types.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data, missing required fields, or email not verified"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Parameter(description = "Complete registration data - email must be verified from step 2", required = true)
            @Valid @RequestBody UserRegistrationDto request) {
        authService.registerAfterEmailVerification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @Operation(summary = "User login with password", description = "Authenticate user with username/email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequestDto dto) {
        JwtResponseDto resp = authService.authenticate(dto);
        return ResponseEntity.ok(resp);
    }

    // OTP endpoints
    @Operation(summary = "Send registration OTP", description = "Send OTP for user registration via email or SMS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/send-registration-otp")
    public ResponseEntity<?> sendRegistrationOtp(
            @Parameter(description = "OTP request details", required = true)
            @Valid @RequestBody OtpRequestDto request) {
        authService.sendRegistrationOtp(request);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @Operation(summary = "Verify registration OTP", description = "Verify OTP for user registration - marks email as verified for final registration step")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully - email marked as verified"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(
            @Parameter(description = "OTP verification details", required = true)
            @Valid @RequestBody RegistrationVerificationDto dto) {
        boolean verified = authService.verifyRegistrationOtpSimple(dto.getIdentifier(), dto.getOtpCode());
        if (verified) {
            return ResponseEntity.ok("OTP verified successfully - you can now proceed with registration");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    @Operation(summary = "Send login OTP", description = "Send OTP for login via email or SMS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/send-login-otp")
    public ResponseEntity<?> sendLoginOtp(
            @Parameter(description = "Login OTP request", required = true)
            @Valid @RequestBody LoginOtpRequestDto request) {
        authService.sendLoginOtp(request);
        return ResponseEntity.ok("OTP sent successfully");
    }

    @Operation(summary = "Verify login OTP", description = "Verify OTP for login and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-login-otp")
    public ResponseEntity<JwtResponseDto> verifyLoginOtp(
            @Parameter(description = "OTP verification details", required = true)
            @Valid @RequestBody OtpVerificationDto dto) {
        JwtResponseDto resp = authService.authenticateWithOtp(dto);
        return ResponseEntity.ok(resp);
    }

    // Forgot password endpoints
    @Operation(summary = "Send forgot password OTP", description = "Send OTP for password reset via email or SMS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent for password reset"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/send-forgot-password-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(
            @Parameter(description = "Forgot password request", required = true)
            @Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.sendForgotPasswordOtp(request);
        return ResponseEntity.ok("OTP sent for password reset");
    }

    @Operation(summary = "Reset password", description = "Reset password with OTP verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Password reset details", required = true)
            @Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }
}

