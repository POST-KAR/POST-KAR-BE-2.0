package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.*;
import com.postkar.project3dmodel.response.RegistrationResponse;
import com.postkar.project3dmodel.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Send OTP to Email", description = "Send verification OTP to the provided email address for registration or login")
    @PostMapping("/send-otp")
    public ResponseEntity<RegistrationResponse> sendOtpToEmail(@Valid @RequestBody EmailRequest req) {
        try {
            RegistrationResponse response = authService.sendOtpToEmail(req);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to send OTP: " + e.getMessage(),
                            req.getEmail(), false));
        }
    }

    @Operation(summary = "Verify OTP and Login/Register", description = "Verify the OTP sent to the email address. Completes registration for new users or logs in existing users.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody EmailOTPVerificationRequest req) {
        try {
            Map<String, Object> response = authService.verifyOTP(req);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "OTP verification failed: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Resend OTP", description = "Resend verification OTP to the email address")
    @PostMapping("/resend-otp")
    public ResponseEntity<RegistrationResponse> resendOtp(@Valid @RequestBody EmailRequest req) {
        try {
            RegistrationResponse response = authService.resendOtp(req);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to resend OTP: " + e.getMessage(),
                            req.getEmail(), false));
        }
    }

    @Operation(summary = "Refresh Token", description = "Generate new access token using refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = authService.refreshToken(request);
            Map<String, String> response = Map.of("accessToken", newAccessToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token refresh failed: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Get User Profile", description = "Get the current authenticated user's profile information")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication auth) {
        try {
            String email = auth.getName();
            Map<String, Object> response = authService.getUserProfile(email);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Failed to get profile: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Update User Profile", description = "Update the current authenticated user's profile information")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @Valid @RequestBody Map<String, String> updates,
            Authentication auth) {
        try {
            String email = auth.getName();
            Map<String, Object> response = authService.updateUserProfile(email, updates);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Failed to update profile: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Logout User", description = "Logout the current authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth) {
        try {
            String email = auth.getName();
            authService.logout(email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Logout failed: " + e.getMessage(),
                            "success", false));
        }
    }
}