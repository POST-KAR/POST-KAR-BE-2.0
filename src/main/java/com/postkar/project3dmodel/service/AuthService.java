package com.postkar.project3dmodel.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.postkar.project3dmodel.response.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.postkar.project3dmodel.dto.*;
import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import com.postkar.project3dmodel.security.JwtTokenProvider;
import com.postkar.project3dmodel.util.OTPUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private Msg91Service msg91Service;

    @Autowired
    private JwtTokenProvider jwtProvider;

    // Step 1: Send OTP to Mobile Number (for both registration and login)
    @Transactional
    public RegistrationResponse sendOtpToMobile(MobileRequest req) {
        String mobileNumber = req.getMobileNumber();

        // Check if user exists
        User existingUser = userRepo.findByPhoneNumber(mobileNumber).orElse(null);
        
        String otp = OTPUtil.generateOTP();
        
        if (existingUser != null) {
            // User exists - this is a login attempt
            existingUser.setOtp(otp);
            existingUser.setOtpGeneratedAt(LocalDateTime.now());
            existingUser.setUpdatedAt(LocalDateTime.now());
            userRepo.save(existingUser);
            
            // Send OTP via MSG91
            boolean otpSent = msg91Service.sendOtp(mobileNumber, otp);
            if (!otpSent) {
                throw new RuntimeException("Failed to send OTP. Please try again.");
            }
            
            return new RegistrationResponse("OTP sent to your mobile number for login", mobileNumber, true);
        } else {
            // New user - this is a registration attempt
            User newUser = new User();
            newUser.setPhoneNumber(mobileNumber);
            newUser.setOtp(otp);
            newUser.setOtpGeneratedAt(LocalDateTime.now());
            newUser.setMobileVerified(false);
            newUser.setActive(false);
            newUser.setProvider("LOCAL");
            newUser.setRegistrationStatus(User.RegistrationStatus.MOBILE_PENDING);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());
            
            userRepo.save(newUser);
            
            // Send OTP via MSG91
            boolean otpSent = msg91Service.sendOtp(mobileNumber, otp);
            if (!otpSent) {
                // Clean up the user if OTP sending failed
                userRepo.delete(newUser);
                throw new RuntimeException("Failed to send OTP. Please try again.");
            }
            
            return new RegistrationResponse("OTP sent to your mobile number for registration", mobileNumber, true);
        }
    }

    // Step 2: Verify OTP (completes registration for new users, logs in existing users)
    @Transactional
    public Map<String, Object> verifyOTP(MobileOTPVerificationRequest req) {
        User user = userRepo.findByPhoneNumber(req.getMobileNumber())
                .orElseThrow(() -> new RuntimeException("User not found. Please request OTP first."));

        // Check OTP expiry (10 minutes)
        if (user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request a new one.");
        }

        // Verify OTP
        if (!user.getOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Clear OTP for security
        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        user.setMobileVerified(true);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());

        // If this was a new registration, update status
        if (user.getRegistrationStatus() == User.RegistrationStatus.MOBILE_PENDING) {
            user.setRegistrationStatus(User.RegistrationStatus.COMPLETED);
        }

        // Generate JWT tokens
        String accessToken = jwtProvider.generateAccessToken(user.getPhoneNumber());
        String refreshToken = jwtProvider.generateRefreshToken(user.getPhoneNumber());

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7)); // 7 days expiry
        
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of(
                "mobileNumber", user.getPhoneNumber(),
                "isActive", user.isActive(),
                "mobileVerified", user.isMobileVerified()
        ));
        response.put("message", user.getRegistrationStatus() == User.RegistrationStatus.COMPLETED ? 
                "Login successful" : "Registration completed and logged in");
        
        return response;
    }

    // Resend OTP
    @Transactional
    public RegistrationResponse resendOtp(MobileRequest req) {
        User user = userRepo.findByPhoneNumber(req.getMobileNumber())
                .orElseThrow(() -> new RuntimeException("User not found. Please request OTP first."));

        // Generate new OTP
        String newOtp = OTPUtil.generateOTP();
        user.setOtp(newOtp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepo.save(user);

        // Send OTP via MSG91
        boolean otpSent = msg91Service.sendOtp(req.getMobileNumber(), newOtp);
        if (!otpSent) {
            throw new RuntimeException("Failed to resend OTP. Please try again.");
        }

        return new RegistrationResponse("OTP resent to your mobile number", req.getMobileNumber(), true);
    }

    // Refresh access token method
    public String refreshToken(RefreshTokenRequest req) {
        User user = userRepo.findByPhoneNumber(req.getEmail()) // Note: using email field for mobile number in existing DTO
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtProvider.validateToken(req.getRefreshToken()) ||
                !req.getRefreshToken().equals(user.getRefreshToken()) ||
                user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user.getPhoneNumber());
        user.setJwtToken(newAccessToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        return newAccessToken;
    }

    @Transactional
    public void cleanupExpiredOtps() {
        // Find users with expired OTPs and clear them
        userRepo.findAll().stream()
                .filter(user -> user.getOtpGeneratedAt() != null && 
                        user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now()))
                .forEach(user -> {
                    user.setOtp(null);
                    user.setOtpGeneratedAt(null);
                    userRepo.save(user);
                });
    }
}