package com.postkar.project3dmodel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.postkar.project3dmodel.dto.EmailVerificationRequest;
import com.postkar.project3dmodel.dto.LoginRequest;
import com.postkar.project3dmodel.dto.RefreshTokenRequest;
import com.postkar.project3dmodel.dto.SignupRequest;
import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import com.postkar.project3dmodel.security.JwtTokenProvider;
import com.postkar.project3dmodel.util.OTPUtil;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public void register(SignupRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setPhoneNumber(req.getPhoneNumber());
        user.setDateOfBirth(LocalDate.parse(req.getDob()));
        user.setProvider("LOCAL");
        user.setEmailVerified(false);

        String otp = OTPUtil.generateOTP();
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());

        userRepo.save(user);
        emailService.sendOtpAsync(user.getEmail(), otp);
    }

    public void verifyEmail(EmailVerificationRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!user.getOtp().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedAt(null);
        userRepo.save(user);
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email not verified");
        }

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of("email", user.getEmail(), "name", user.getName()));
        return response;
    }

    public void resendOtp(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) throw new RuntimeException("Email already verified");

        if (user.getOtpGeneratedAt().plusMinutes(10).isAfter(LocalDateTime.now())) {
            emailService.sendOtpAsync(email, user.getOtp());
        } else {
            String newOtp = OTPUtil.generateOTP();
            user.setOtp(newOtp);
            user.setOtpGeneratedAt(LocalDateTime.now());
            userRepo.save(user);
            emailService.sendOtpAsync(email, newOtp);
        }
    }

    public String refreshToken(RefreshTokenRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtProvider.validateToken(req.getRefreshToken()) || !req.getRefreshToken().equals(user.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtProvider.generateAccessToken(user.getEmail());
        user.setJwtToken(newAccessToken);
        userRepo.save(user);
        return newAccessToken;
    }
}