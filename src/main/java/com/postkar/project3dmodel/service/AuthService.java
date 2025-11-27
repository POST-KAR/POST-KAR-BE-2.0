package com.postkar.project3dmodel.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.postkar.project3dmodel.response.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    // Step 1: Send OTP to Email (for both registration and login)
    public RegistrationResponse sendOtpToEmail(EmailRequest req) {
        String email = req.getEmail();

        // Check if user exists
        User existingUser = userRepo.findByEmail(email).orElse(null);

        String otp = OTPUtil.generateOTP();

        if (existingUser != null) {
            // User exists - this is a login attempt
            existingUser.setOtp(otp);
            existingUser.setOtpGeneratedAt(LocalDateTime.now());
            existingUser.setUpdatedAt(LocalDateTime.now());
            userRepo.save(existingUser);

            // Send OTP via Email
            boolean otpSent = emailService.sendOtp(email, otp);
            if (!otpSent) {
                throw new RuntimeException("Failed to send OTP. Please try again.");
            }

            return new RegistrationResponse("OTP sent to your email for login", email, true);
        } else {
            // New user - this is a registration attempt
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setOtp(otp);
            newUser.setOtpGeneratedAt(LocalDateTime.now());
            newUser.setEmailVerified(false);
            newUser.setActive(false);
            newUser.setProvider("LOCAL");
            newUser.setRegistrationStatus(User.RegistrationStatus.EMAIL_PENDING);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());

            userRepo.save(newUser);

            // Send OTP via Email
            boolean otpSent = emailService.sendOtp(email, otp);
            if (!otpSent) {
                // Clean up the user if OTP sending failed
                userRepo.delete(newUser);
                throw new RuntimeException("Failed to send OTP. Please try again.");
            }

            return new RegistrationResponse("OTP sent to your email for registration", email, true);
        }
    }

    // Step 2: Verify OTP (completes registration for new users, logs in existing
    // users)
    public Map<String, Object> verifyOTP(EmailOTPVerificationRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
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
        user.setEmailVerified(true);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());

        // If this was a new registration, update status
        if (user.getRegistrationStatus() == User.RegistrationStatus.EMAIL_PENDING) {
            user.setRegistrationStatus(User.RegistrationStatus.COMPLETED);
        }

        // Generate JWT tokens
        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7)); // 7 days expiry

        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of(
                "email", user.getEmail(),
                "isActive", user.isActive(),
                "emailVerified", user.isEmailVerified()));
        response.put("message", user.getRegistrationStatus() == User.RegistrationStatus.COMPLETED ? "Login successful"
                : "Registration completed and logged in");

        return response;
    }

    // Resend OTP
    public RegistrationResponse resendOtp(EmailRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found. Please request OTP first."));

        // Generate new OTP
        String newOtp = OTPUtil.generateOTP();
        user.setOtp(newOtp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        // Send OTP via Email
        boolean otpSent = emailService.sendOtp(req.getEmail(), newOtp);
        if (!otpSent) {
            throw new RuntimeException("Failed to resend OTP. Please try again.");
        }

        return new RegistrationResponse("OTP resent to your email", req.getEmail(), true);
    }

    // Refresh access token method
    public String refreshToken(RefreshTokenRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtProvider.validateToken(req.getRefreshToken()) ||
                !req.getRefreshToken().equals(user.getRefreshToken()) ||
                user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        user.setJwtToken(newAccessToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        return newAccessToken;
    }

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

    // Get user profile
    public Map<String, Object> getUserProfile(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt()));
        return response;
    }

    // Update user profile
    public Map<String, Object> updateUserProfile(String email, Map<String, String> updates) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt()));
        return response;
    }

    // Logout user
    public void logout(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setJwtToken(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
    }
}