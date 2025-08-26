package com.postkar.project3dmodel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOtpAsync(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your OTP for Post - कAR Email Verification");
            message.setText("Your OTP is: " + otp);
            mailSender.send(message);
            logger.info("OTP sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}: {}", to, e.getMessage());
        }
    }

    public void sendOtp(String to, String otp) {
        sendOtpAsync(to, otp);
    }
}