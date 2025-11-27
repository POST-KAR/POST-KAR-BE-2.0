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
            System.out.println("=== SEND OTP REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail());

            RegistrationResponse response = authService.sendOtpToEmail(req);

            System.out.println("OTP sent successfully to: " + req.getEmail());
            System.out.println("=== SEND OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== SEND OTP REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());
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
            System.out.println("=== VERIFY OTP REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail() + ", OTP: " + req.getOtp());

            Map<String, Object> response = authService.verifyOTP(req);

            System.out.println("OTP verified successfully for: " + req.getEmail());
            System.out.println("=== VERIFY OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== VERIFY OTP REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "OTP verification failed: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Resend OTP", description = "Resend verification OTP to the email address")
    @PostMapping("/resend-otp")
    public ResponseEntity<RegistrationResponse> resendOtp(@Valid @RequestBody EmailRequest req) {
        try {
            System.out.println("=== RESEND OTP REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail());

            RegistrationResponse response = authService.resendOtp(req);

            System.out.println("OTP resent successfully to: " + req.getEmail());
            System.out.println("=== RESEND OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== RESEND OTP REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

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
            System.out.println("=== REFRESH TOKEN REQUEST STARTED ===");
            System.out.println("Email: " + request.getEmail());

            String newAccessToken = authService.refreshToken(request);
            Map<String, String> response = Map.of("accessToken", newAccessToken);

            System.out.println("Token refreshed successfully for: " + request.getEmail());
            System.out.println("=== REFRESH TOKEN REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== REFRESH TOKEN REQUEST FAILED ===");
            System.err.println("Error for email: " + request.getEmail() + " - " + e.getMessage());

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
            System.out.println("=== GET PROFILE REQUEST STARTED ===");
            System.out.println("Email: " + email);

            Map<String, Object> response = authService.getUserProfile(email);

            System.out.println("Profile retrieved successfully");
            System.out.println("=== GET PROFILE REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== GET PROFILE REQUEST FAILED ===");
            System.err.println("Error: " + e.getMessage());

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
            System.out.println("=== UPDATE PROFILE REQUEST STARTED ===");
            System.out.println("Email: " + email);

            Map<String, Object> response = authService.updateUserProfile(email, updates);

            System.out.println("Profile updated successfully");
            System.out.println("=== UPDATE PROFILE REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== UPDATE PROFILE REQUEST FAILED ===");
            System.err.println("Error: " + e.getMessage());

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
            System.out.println("=== LOGOUT REQUEST STARTED ===");
            System.out.println("Email: " + email);

            authService.logout(email);

            System.out.println("Logout successful");
            System.out.println("=== LOGOUT REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Logged out successfully"));
        } catch (Exception e) {
            System.err.println("=== LOGOUT REQUEST FAILED ===");
            System.err.println("Error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Logout failed: " + e.getMessage(),
                            "success", false));
        }
    }
}