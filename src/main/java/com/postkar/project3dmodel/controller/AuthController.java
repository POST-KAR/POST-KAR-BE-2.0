package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.EmailVerificationRequest;
import com.postkar.project3dmodel.dto.LoginRequest;
import com.postkar.project3dmodel.dto.RefreshTokenRequest;
import com.postkar.project3dmodel.dto.SignupRequest;
import com.postkar.project3dmodel.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> response = authService.login(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody EmailVerificationRequest req) {
        authService.resendOtp(req.getEmail());
        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshTokenRequest request) {
        String newAccessToken = authService.refreshToken(request);
        Map<String, String> response = Map.of("accessToken", newAccessToken);
        return ResponseEntity.ok(response);
    }
}
