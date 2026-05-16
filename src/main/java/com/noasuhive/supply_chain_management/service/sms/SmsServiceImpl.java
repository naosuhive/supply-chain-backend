package com.noasuhive.supply_chain_management.service.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.api.url}")
    private String smsApiUrl;

    @Value("${sms.api.key}")
    private String smsApiKey;

    @Value("${sms.sender.id}")
    private String smsSenderId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendOtpSms(String phoneNumber, String otp) {
        try {
            // For Trail SMS demo account, we need to use the specific format
            String message = "Hello " + otp + ", This is a test message from spring edge";
            
            // Build the URL with proper encoding
            String url = String.format("%s?apikey=%s&sender=%s&to=%s&message=%s",
                    smsApiUrl,
                    smsApiKey,
                    smsSenderId,
                    phoneNumber,
                    java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8)
            );

            // Make the API call
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("SMS sent successfully to " + phoneNumber + ". Response: " + response);
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("Failed to send SMS to " + phoneNumber, e);
        }
    }
}
