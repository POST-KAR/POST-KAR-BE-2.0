package com.postkar.project3dmodel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.postkar.project3dmodel.response.RegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.postkar.project3dmodel.dto.*;
import com.postkar.project3dmodel.entity.TempRegistration;
import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.TempRegistrationRepository;
import com.postkar.project3dmodel.repository.UserRepository;
import com.postkar.project3dmodel.security.JwtTokenProvider;
import com.postkar.project3dmodel.util.OTPUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TempRegistrationRepository tempRegRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    // Window 1: Send OTP to Email
    @Transactional
    public RegistrationResponse sendOtpToEmail(EmailRequest req) {
        String email = req.getEmail();

        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        tempRegRepo.deleteByEmail(email);

        TempRegistration tempReg = new TempRegistration();
        tempReg.setEmail(email);
        tempReg.setOtp(OTPUtil.generateOTP());
        tempReg.setOtpGeneratedAt(LocalDateTime.now());
        tempReg.setEmailVerified(false);
        tempReg.setStatus(TempRegistration.Status.EMAIL_SENT);
        tempReg.setCreatedAt(LocalDateTime.now());
        tempReg.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24-hour expiry

        tempRegRepo.save(tempReg);
        emailService.sendOtpAsync(email, tempReg.getOtp());

        return new RegistrationResponse("OTP sent to your email", email, true);
    }

    // Window 2: Verify OTP
    @Transactional
    public RegistrationResponse verifyOTP(OTPVerificationRequest req) {
        TempRegistration tempReg = tempRegRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Registration not found. Please start again."));

        if (tempReg.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!tempReg.getOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        tempReg.setEmailVerified(true);
        tempReg.setStatus(TempRegistration.Status.EMAIL_VERIFIED);
        tempReg.setOtp(null); // Clear OTP for security
        tempReg.setOtpGeneratedAt(null);
        tempRegRepo.save(tempReg);

        return new RegistrationResponse("Email verified successfully", req.getEmail(), true);
    }

    // Window 2: Resend OTP
    @Transactional
    public RegistrationResponse resendOtp(EmailRequest req) {
        TempRegistration tempReg = tempRegRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Registration not found. Please start again."));

        if (tempReg.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }

        if (tempReg.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            String newOtp = OTPUtil.generateOTP();
            tempReg.setOtp(newOtp);
            tempReg.setOtpGeneratedAt(LocalDateTime.now());
            tempRegRepo.save(tempReg);
            emailService.sendOtpAsync(req.getEmail(), newOtp);
        } else {
            emailService.sendOtpAsync(req.getEmail(), tempReg.getOtp());
        }

        return new RegistrationResponse("OTP resent to your email", req.getEmail(), true);
    }

    // Window 3: Set Credentials and Create User
    @Transactional
    public RegistrationResponse setCredentials(CredentialsRequest req) {
        TempRegistration tempReg = tempRegRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Registration not found. Please start again."));

        if (!tempReg.isEmailVerified()) {
            throw new RuntimeException("Email not verified. Please verify your email first.");
        }

        if (userRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepo.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Create the user now instead of waiting for step 4
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setEmailVerified(true);
        user.setCredentialsSet(true);
        user.setProfileCompleted(false); // Will be set to true if they complete step 4
        user.setProvider("LOCAL");
        user.setRegistrationStatus(User.RegistrationStatus.COMPLETED); // User is now registered
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        // Clean up temporary registration
        tempRegRepo.deleteByEmail(req.getEmail());

        return new RegistrationResponse("Registration completed successfully. You can now login or optionally complete your profile.", req.getEmail(), true);
    }

    // Window 4: Set Info (Now Optional)
    @Transactional
    public RegistrationResponse setInfo(PersonalInfoRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found. Please complete registration first."));

        if (!user.isCredentialsSet()) {
            throw new RuntimeException("Please complete your registration first");
        }

        // Update user with personal information
        user.setName(req.getName());
        user.setPhoneNumber(req.getPhoneNumber());

        if (req.getDob() != null && !req.getDob().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(req.getDob()));
        }

        user.setProfileCompleted(true);
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        return new RegistrationResponse("Profile completed successfully", req.getEmail(), true);
    }

    // Login method - Now allows login even if profile is not completed
    public Map<String, Object> login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if (!user.isCredentialsSet()) {
            throw new RuntimeException("Please complete your registration");
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of(
                "email", user.getEmail(),
                "name", user.getName() != null ? user.getName() : "",
                "username", user.getUsername(),
                "profileCompleted", user.isProfileCompleted()
        ));
        return response;
    }

    // Refresh access token method
    public String refreshToken(RefreshTokenRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtProvider.validateToken(req.getRefreshToken()) ||
                !req.getRefreshToken().equals(user.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        user.setJwtToken(newAccessToken);
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
        return newAccessToken;
    }

    @Transactional
    public void cleanupExpiredRegistrations() {
        tempRegRepo.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}