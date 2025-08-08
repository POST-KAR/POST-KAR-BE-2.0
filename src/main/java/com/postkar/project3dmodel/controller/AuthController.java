package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.EmailVerificationRequest;
import com.postkar.project3dmodel.dto.LoginRequest;
import com.postkar.project3dmodel.dto.RefreshTokenRequest;
import com.postkar.project3dmodel.dto.SignupRequest;
import com.postkar.project3dmodel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User signup, login, and authentication APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "User Signup", description = "Register a new user in the system")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest req) {
        authService.register(req);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @Operation(summary = "Verify Email OTP", description = "Verify user's email with the provided OTP")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verify(@RequestBody EmailVerificationRequest req) {
        authService.verifyEmail(req);
        return ResponseEntity.ok("Email verified successfully");
    }

    @Operation(summary = "User Login", description = "Authenticate a user and return JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        Map<String, Object> response = authService.login(req);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resend OTP", description = "Resend verification OTP to user's email")
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody EmailVerificationRequest req) {
        authService.resendOtp(req.getEmail());
        return ResponseEntity.ok("OTP sent");
    }

    @Operation(summary = "Refresh Token", description = "Regenerate access and refresh tokens using the refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshTokenRequest request) {
        String newAccessToken = authService.refreshToken(request);
        Map<String, String> response = Map.of("accessToken", newAccessToken);
        return ResponseEntity.ok(response);
    }
}
