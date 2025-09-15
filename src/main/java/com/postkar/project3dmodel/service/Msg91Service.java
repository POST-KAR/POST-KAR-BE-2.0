package com.postkar.project3dmodel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Msg91Service {

    private static final Logger logger = LoggerFactory.getLogger(Msg91Service.class);
    
    @Value("${msg91.auth.key}")
    private String authKey;
    
    @Value("${msg91.template.id}")
    private String templateId;
    
    @Value("${msg91.sender.id:MSGIND}")
    private String senderId;
    
    private static final String MSG91_BASE_URL = "https://control.msg91.com/api/v5";
    private static final String SEND_OTP_ENDPOINT = "/otp";
    private static final String VERIFY_OTP_ENDPOINT = "/otp/verify";
    private static final String RESEND_OTP_ENDPOINT = "/otp/retry";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Send OTP to mobile number
     */
    public boolean sendOtp(String mobileNumber, String otp) {
        try {
            String url = MSG91_BASE_URL + SEND_OTP_ENDPOINT;
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("template_id", templateId);
            payload.put("mobile", mobileNumber);
            payload.put("authkey", authKey);
            payload.put("otp", otp);
            payload.put("sender", senderId);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("authkey", authKey);
                httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
                
                var response = httpClient.execute(httpPost);
                int statusCode = response.getCode();
                
                logger.info("MSG91 Send OTP Response - Status: {}, Mobile: {}", statusCode, mobileNumber);
                
                return statusCode == 200;
            }
        } catch (Exception e) {
            logger.error("Error sending OTP via MSG91 to mobile: {}", mobileNumber, e);
            return false;
        }
    }
    
    /**
     * Verify OTP with MSG91
     */
    public boolean verifyOtp(String mobileNumber, String otp) {
        try {
            String url = MSG91_BASE_URL + VERIFY_OTP_ENDPOINT;
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("authkey", authKey);
            payload.put("mobile", mobileNumber);
            payload.put("otp", otp);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("authkey", authKey);
                httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
                
                var response = httpClient.execute(httpPost);
                int statusCode = response.getCode();
                
                logger.info("MSG91 Verify OTP Response - Status: {}, Mobile: {}", statusCode, mobileNumber);
                
                return statusCode == 200;
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP via MSG91 for mobile: {}", mobileNumber, e);
            return false;
        }
    }
    
    /**
     * Resend OTP via MSG91
     */
    public boolean resendOtp(String mobileNumber) {
        try {
            String url = MSG91_BASE_URL + RESEND_OTP_ENDPOINT;
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("authkey", authKey);
            payload.put("mobile", mobileNumber);
            payload.put("retrytype", "text");
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("authkey", authKey);
                httpPost.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));
                
                var response = httpClient.execute(httpPost);
                int statusCode = response.getCode();
                
                logger.info("MSG91 Resend OTP Response - Status: {}, Mobile: {}", statusCode, mobileNumber);
                
                return statusCode == 200;
            }
        } catch (Exception e) {
            logger.error("Error resending OTP via MSG91 for mobile: {}", mobileNumber, e);
            return false;
        }
    }
}
