package com.postkar.project3dmodel.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.postkar.project3dmodel.dto.EmailVerificationRequest;
import com.postkar.project3dmodel.dto.LoginRequest;
import com.postkar.project3dmodel.dto.RefreshTokenRequest;
import com.postkar.project3dmodel.dto.SignupRequest;
import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import com.postkar.project3dmodel.security.JwtTokenProvider;
import com.postkar.project3dmodel.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    @Autowired
    private UserRepository userRepo;


    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest req) {
        authService.register(req);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verify(@RequestBody EmailVerificationRequest req) {
        authService.verifyEmail(req);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {
        Map<String, String> tokens = authService.login(req);
        return ResponseEntity.ok(tokens);
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody EmailVerificationRequest req) {
        authService.resendOtp(req.getEmail());
        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(oldRefreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        String email = jwtProvider.extractEmail(oldRefreshToken);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!oldRefreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch");
        }

        String newAccessToken = jwtProvider.generateAccessToken(email);
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        user.setJwtToken(newAccessToken);
        user.setRefreshToken(newRefreshToken);
        userRepo.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRefreshToken);
        return ResponseEntity.ok(tokens);
    }


}
