package com.noasuhive.supply_chain_management.service.auth;

import com.noasuhive.supply_chain_management.dto.auth.*;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.RetailerProfile;
import com.noasuhive.supply_chain_management.models.Role;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.RoleRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.otp.OtpService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ManufacturerProfileRepository manufacturerProfileRepository;
    private final RetailerProfileRepository retailerProfileRepository;
    private final OtpService otpService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider, ManufacturerProfileRepository manufacturerProfileRepository,
                           RetailerProfileRepository retailerProfileRepository, OtpService otpService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.manufacturerProfileRepository = manufacturerProfileRepository;
        this.retailerProfileRepository = retailerProfileRepository;
        this.otpService = otpService;
    }

    @Override
    @Transactional
    public void register(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number is already in use");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        Role role = roleRepository.findByName(dto.getRole())
                .orElseGet(() -> roleRepository.save(new Role(dto.getRole())));

        user.setRoles(Collections.singleton(role));
        userRepository.save(user);

        // Create profile for one-to-one mapping
        if ("ROLE_MANUFACTURER".equals(dto.getRole())) {
            ManufacturerProfile mp = new ManufacturerProfile();
            mp.setUserId(user.getId());
            mp.setCompanyName("");
            manufacturerProfileRepository.save(mp);
        } else if ("ROLE_RETAILER".equals(dto.getRole())) {
            RetailerProfile rp = new RetailerProfile();
            rp.setUserId(user.getId());
            rp.setBusinessName("");
            retailerProfileRepository.save(rp);
        }
    }

    @Override
    public JwtResponseDto authenticate(LoginRequestDto dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsernameOrEmail(), dto.getPassword())
            );

            // find user by username or email
            User user = userRepository.findByUsernameOrEmailOrPhoneNumber(dto.getUsernameOrEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Map<String, Object> claims = new HashMap<>();
            claims.put("user_id", user.getId().toString());
            // include roles
            claims.put("roles", user.getRoles().stream().map(Role::getName).toList());
            // include profile id for manufacturer or retailer if present
            if (user.getRoles().stream().anyMatch(r -> "ROLE_MANUFACTURER".equals(r.getName()))) {
                manufacturerProfileRepository.findByUserId(user.getId()).ifPresent(mp -> claims.put("manufacturer_id", mp.getId().toString()));
            }
            if (user.getRoles().stream().anyMatch(r -> "ROLE_RETAILER".equals(r.getName()))) {
                retailerProfileRepository.findByUserId(user.getId()).ifPresent(rp -> claims.put("retailer_id", rp.getId().toString()));
            }

            String token = tokenProvider.createToken(user.getUsername(), claims);
            return new JwtResponseDto(token);
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Invalid username, email, phone number or password");
        }
    }

    @Override
    public void sendRegistrationOtp(OtpRequestDto request) {
        otpService.sendRegistrationOtp(request);
    }

    @Override
    public void verifyRegistrationOtp(OtpVerificationDto dto) {
        if (!otpService.verifyOtp(dto)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        // Registration OTP verified - user can now complete registration
    }

    @Override
    public void sendLoginOtp(LoginOtpRequestDto request) {
        otpService.sendLoginOtp(request.getIdentifier());
    }

    @Override
    public JwtResponseDto authenticateWithOtp(OtpVerificationDto dto) {
        if (!otpService.verifyOtp(dto)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }

        User user = userRepository.findByUsernameOrEmailOrPhoneNumber(dto.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return generateJwtResponse(user);
    }

    @Override
    public void sendForgotPasswordOtp(ForgotPasswordRequestDto request) {
        // Clean up the identifier
        String identifier = request.getIdentifier().trim();
        
        // Check if user exists
        User user = userRepository.findByUsernameOrEmailOrPhoneNumber(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with identifier: " + identifier));
        
        // Send OTP using the same logic as login OTP
        otpService.sendLoginOtp(identifier);
        System.out.println("Forgot password OTP sent for: " + identifier);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto request) {
        // Clean up the identifier
        String identifier = request.getIdentifier().trim();
        
        // Verify OTP first
        OtpVerificationDto otpDto = new OtpVerificationDto(identifier, request.getOtpCode());
        if (!otpService.verifyOtp(otpDto)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        
        // Find user
        User user = userRepository.findByUsernameOrEmailOrPhoneNumber(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        System.out.println("Password reset successfully for: " + identifier);
    }

    private JwtResponseDto generateJwtResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId().toString());
        claims.put("roles", user.getRoles().stream().map(Role::getName).toList());
        
        // include profile id for manufacturer or retailer if present
        if (user.getRoles().stream().anyMatch(r -> "ROLE_MANUFACTURER".equals(r.getName()))) {
            manufacturerProfileRepository.findByUserId(user.getId()).ifPresent(mp -> claims.put("manufacturer_id", mp.getId().toString()));
        }
        if (user.getRoles().stream().anyMatch(r -> "ROLE_RETAILER".equals(r.getName()))) {
            retailerProfileRepository.findByUserId(user.getId()).ifPresent(rp -> claims.put("retailer_id", rp.getId().toString()));
        }

        String token = tokenProvider.createToken(user.getUsername(), claims);
        return new JwtResponseDto(token);
    }
}
