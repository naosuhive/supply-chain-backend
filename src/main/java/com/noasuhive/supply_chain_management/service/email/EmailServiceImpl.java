package com.noasuhive.supply_chain_management.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code - Supply Chain Management");
            message.setText("Your OTP code is: " + otp + "\n\n" +
                          "This code will expire in 5 minutes.\n" +
                          "Please do not share this code with anyone.\n\n" +
                          "Thank you,\n" +
                          "Supply Chain Management Team");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email to " + toEmail, e);
        }
    }
}
