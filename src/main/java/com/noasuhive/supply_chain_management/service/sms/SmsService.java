package com.noasuhive.supply_chain_management.service.sms;

public interface SmsService {
    void sendOtpSms(String phoneNumber, String otp);
}
