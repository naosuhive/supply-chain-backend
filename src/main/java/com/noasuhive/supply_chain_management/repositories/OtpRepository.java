package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<OtpToken, UUID> {

    // Find OTP by phone number or email
    @Query("SELECT o FROM OtpToken o WHERE (o.phoneNumber = :identifier OR o.email = :identifier) AND o.otpCode = :otpCode AND o.isUsed = false AND o.expiryTime > :now")
    Optional<OtpToken> findValidOtp(@Param("identifier") String identifier, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);

    // Find OTP by phone number
    Optional<OtpToken> findByPhoneNumberAndIsUsedFalseAndExpiryTimeAfter(String phoneNumber, LocalDateTime now);

    // Find OTP by email
    Optional<OtpToken> findByEmailAndIsUsedFalseAndExpiryTimeAfter(String email, LocalDateTime now);

    // Find OTP by phone number or email (for debugging - any unused OTP)
    @Query("SELECT o FROM OtpToken o WHERE (o.phoneNumber = :identifier OR o.email = :identifier) AND o.isUsed = false AND o.expiryTime > :now")
    Optional<OtpToken> findByEmailOrPhoneNumberAndIsUsedFalse(@Param("identifier") String identifier, @Param("now") LocalDateTime now);

    // Delete expired OTPs
    @Query("DELETE FROM OtpToken o WHERE o.expiryTime < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    // Mark OTP as used
    @Query("UPDATE OtpToken o SET o.isUsed = true WHERE o.id = :id")
    void markAsUsed(@Param("id") UUID id);

    // Find OTP by verification token and identifier
    @Query("SELECT o FROM OtpToken o WHERE o.verificationToken = :verificationToken AND (o.phoneNumber = :identifier OR o.email = :identifier) AND o.isUsed = false")
    Optional<OtpToken> findByVerificationTokenAndIdentifier(@Param("verificationToken") String verificationToken, @Param("identifier") String identifier);

    // Find OTP token with email verified status
    @Query("SELECT o FROM OtpToken o WHERE (o.phoneNumber = :identifier OR o.email = :identifier) AND o.emailVerified = true AND o.isUsed = false AND o.expiryTime > :now")
    Optional<OtpToken> findEmailVerifiedOtp(@Param("identifier") String identifier, @Param("now") LocalDateTime now);
}
