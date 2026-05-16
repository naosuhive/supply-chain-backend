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
import java.util.UUID;

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
    @Transactional
    public boolean verifyRegistrationOtp(String identifier, String otpCode) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Verifying registration OTP for: " + cleanIdentifier + " with code: " + otpCode);
        
        Optional<OtpToken> otpToken = otpRepository.findValidOtp(cleanIdentifier, otpCode, LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            System.out.println("Registration OTP verified successfully for: " + cleanIdentifier);
            // Don't mark as used yet - will be marked after registration is complete
            return true;
        } else {
            System.out.println("Registration OTP not found, expired, or already used for: " + cleanIdentifier);
            return false;
        }
    }

    @Override
    @Transactional
    public String verifyRegistrationOtpWithToken(String identifier, String otpCode) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Verifying registration OTP with token for: " + cleanIdentifier + " with code: " + otpCode);
        
        Optional<OtpToken> otpToken = otpRepository.findValidOtp(cleanIdentifier, otpCode, LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            // Generate a temporary verification token (valid for 15 minutes)
            String verificationToken = UUID.randomUUID().toString();
            
            // Store the token in the existing OTP token's metadata (we can add a new field)
            // For now, we'll use a simple approach: append to the existing OTP
            OtpToken token = otpToken.get();
            token.setVerificationToken(verificationToken);
            token.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
            otpRepository.save(token);
            
            System.out.println("Registration OTP verified with token for: " + cleanIdentifier);
            return verificationToken;
        } else {
            System.out.println("Registration OTP not found, expired, or already used for: " + cleanIdentifier);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean verifyRegistrationOtpSimple(String identifier, String otpCode) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Verifying registration OTP (simple) for: " + cleanIdentifier + " with code: " + otpCode);
        
        Optional<OtpToken> otpToken = otpRepository.findValidOtp(cleanIdentifier, otpCode, LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            // Mark OTP as verified but don't mark as used yet
            OtpToken token = otpToken.get();
            token.setEmailVerified(true); // Add this field to mark email as verified
            otpRepository.save(token);
            
            System.out.println("Registration OTP verified successfully for: " + cleanIdentifier);
            return true;
        } else {
            System.out.println("Registration OTP not found, expired, or already used for: " + cleanIdentifier);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean isEmailVerified(String identifier) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Checking if email is verified for: " + cleanIdentifier);
        
        // Find any unused OTP token for this identifier that has email verified
        Optional<OtpToken> otpToken = otpRepository.findEmailVerifiedOtp(cleanIdentifier, LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            OtpToken token = otpToken.get();
            
            // Check if verification is still valid (OTP not expired)
            if (!token.isExpired()) {
                System.out.println("Email is verified for: " + cleanIdentifier);
                return true;
            } else {
                System.out.println("Email verification expired for: " + cleanIdentifier);
                return false;
            }
        } else {
            System.out.println("Email not verified for: " + cleanIdentifier);
            return false;
        }
    }

    @Override
    @Transactional
    public void markOtpAsUsed(String identifier, String otpCode) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Marking OTP as used for: " + cleanIdentifier);
        
        Optional<OtpToken> otpToken = otpRepository.findValidOtp(cleanIdentifier, otpCode, LocalDateTime.now());
        
        if (otpToken.isPresent()) {
            OtpToken token = otpToken.get();
            token.setUsed(true);
            otpRepository.save(token);
            System.out.println("OTP marked as used successfully for: " + cleanIdentifier);
        } else {
            System.out.println("No valid OTP found to mark as used for: " + cleanIdentifier);
        }
    }

    @Override
    @Transactional
    public boolean validateVerificationToken(String verificationToken, String identifier) {
        // Clean up the identifier
        String cleanIdentifier = identifier.trim();
        
        System.out.println("Validating verification token for: " + cleanIdentifier);
        
        // Find OTP token by verification token and identifier
        Optional<OtpToken> otpToken = otpRepository.findByVerificationTokenAndIdentifier(verificationToken, cleanIdentifier);
        
        if (otpToken.isPresent()) {
            OtpToken token = otpToken.get();
            
            // Check if verification token is still valid
            if (token.isVerificationTokenExpired()) {
                System.out.println("Verification token expired for: " + cleanIdentifier);
                return false;
            }
            
            System.out.println("Verification token valid for: " + cleanIdentifier);
            return true;
        } else {
            System.out.println("Invalid verification token for: " + cleanIdentifier);
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
