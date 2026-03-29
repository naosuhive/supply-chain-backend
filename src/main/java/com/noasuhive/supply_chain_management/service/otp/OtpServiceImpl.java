package com.noasuhive.supply_chain_management.service.otp;

import com.noasuhive.supply_chain_management.dto.auth.OtpRequestDto;
import com.noasuhive.supply_chain_management.dto.auth.OtpVerificationDto;
import com.noasuhive.supply_chain_management.models.OtpToken;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.OtpRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import com.noasuhive.supply_chain_management.service.email.EmailService;
import com.noasuhive.supply_chain_management.service.sms.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.length:6}")
    private int otpLength;

    public OtpServiceImpl(OtpRepository otpRepository, UserRepository userRepository, 
                         EmailService emailService, SmsService smsService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    @Transactional
    public void sendRegistrationOtp(OtpRequestDto request) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Clean up email and phone
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        
        // Store OTP with email and phone
        OtpToken otpToken = new OtpToken(phone, email, otp, expiryTime);
        otpRepository.save(otpToken);
        
        // Auto-detect which field to send OTP to based on what's provided
        if (email != null && !email.isEmpty()) {
            // Send OTP to email
            emailService.sendOtpEmail(email, otp);
            System.out.println("Registration OTP sent to email: " + email);
        } else if (phone != null && !phone.isEmpty()) {
            // Send OTP to phone
            smsService.sendOtpSms(phone, otp);
            System.out.println("Registration OTP sent to phone: " + phone);
        } else {
            throw new IllegalArgumentException("Either email or phone number must be provided");
        }
    }

    @Override
    @Transactional
    public void sendLoginOtp(String identifier) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        User user = userRepository.findByUsernameOrEmailOrPhoneNumber(cleanIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with identifier: " + cleanIdentifier));
        
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        
        // Store OTP with email and phone
        OtpToken otpToken = new OtpToken(user.getPhoneNumber(), user.getEmail(), otp, expiryTime);
        otpRepository.save(otpToken);
        
        // Determine if identifier is email or phone number and send OTP accordingly
        if (cleanIdentifier.contains("@")) {
            // Login via email - send OTP to email
            emailService.sendOtpEmail(user.getEmail(), otp);
            System.out.println("Login OTP sent to email: " + user.getEmail());
        } else {
            // Login via phone number - send OTP via SMS
            smsService.sendOtpSms(user.getPhoneNumber(), otp);
            System.out.println("Login OTP sent to phone: " + user.getPhoneNumber());
        }
    }

    @Override
    @Transactional
    public boolean verifyOtp(OtpVerificationDto dto) {
        // Clean up the identifier
        String identifier = dto.getIdentifier().trim();
        
        System.out.println("Attempting to verify OTP for: " + identifier + " with code: " + dto.getOtpCode());
        
        Optional<OtpToken> otpToken = otpRepository.findValidOtp(identifier, dto.getOtpCode(), LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            System.out.println("OTP found and is valid. Marking as used.");
            // Mark OTP as used ONLY when successfully verified
            OtpToken token = otpToken.get();
            token.setUsed(true);
            otpRepository.save(token);
            return true;
        } else {
            System.out.println("OTP not found, expired, or already used for: " + identifier);
            // Check if there's an unused OTP for this identifier (for debugging)
            Optional<OtpToken> anyOtp = otpRepository.findByEmailOrPhoneNumberAndIsUsedFalse(identifier, LocalDateTime.now());
            if (anyOtp.isPresent()) {
                System.out.println("Found unused OTP but wrong code. Correct code is: " + anyOtp.get().getOtpCode());
            } else {
                System.out.println("No valid OTP found for this identifier.");
            }
            return false;
        }
    }

    @Override
    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
