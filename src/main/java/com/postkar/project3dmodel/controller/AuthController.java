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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "FOR Slide/Window 1: Send OTP to Email",
            description = "Send verification OTP to the provided email")
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

    @Operation(summary = "FOR Slide/Window 2: Verify OTP",
            description = "Verify the OTP sent to the email address")
    @PostMapping("/verify-otp")
    public ResponseEntity<RegistrationResponse> verifyOTP(@Valid @RequestBody OTPVerificationRequest req) {
        try {
            System.out.println("=== VERIFY OTP REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail() + ", OTP: " + req.getOtp());

            RegistrationResponse response = authService.verifyOTP(req);

            System.out.println("OTP verified successfully for: " + req.getEmail());
            System.out.println("=== VERIFY OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== VERIFY OTP REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("OTP verification failed: " + e.getMessage(),
                            req.getEmail(), false));
        }
    }

    @Operation(summary = "FOR Slide/Window 2: Resend OTP",
            description = "Resend verification OTP to the email address")
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

    @Operation(summary = "FOR Slide/Window 3: Set Username and Password",
            description = "Set username and password for the account")
    @PostMapping("/set-credentials")
    public ResponseEntity<RegistrationResponse> setCredentials(@Valid @RequestBody CredentialsRequest req) {
        try {
            System.out.println("=== SET CREDENTIALS REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail() + ", Username: " + req.getUsername());

            RegistrationResponse response = authService.setCredentials(req);

            System.out.println("Credentials set successfully for: " + req.getEmail());
            System.out.println("=== SET CREDENTIALS REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== SET CREDENTIALS REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to set credentials: " + e.getMessage(),
                            req.getEmail(), false));
        }
    }

    @Operation(summary = "FOR Slide/Window 4: Set Information",
            description = "Complete registration with personal information. Name, DOB, Phone number. Phone number is optional. Use DOB format as yyyy-MM-dd")
    @PostMapping("/set-info")
    public ResponseEntity<RegistrationResponse> setInfo(@Valid @RequestBody PersonalInfoRequest req) {
        try {
            System.out.println("=== COMPLETE REGISTRATION REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail() + ", Name: " + req.getName());

            RegistrationResponse response = authService.setInfo(req);

            System.out.println("Registration completed successfully for: " + req.getEmail());
            System.out.println("=== COMPLETE REGISTRATION REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== COMPLETE REGISTRATION REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to complete registration: " + e.getMessage(),
                            req.getEmail(), false));
        }
    }

    @Operation(summary = "User Login",
            description = "Authenticate user with email and password, returns JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            System.out.println("=== LOGIN REQUEST STARTED ===");
            System.out.println("Email: " + req.getEmail());

            Map<String, Object> response = authService.login(req);

            System.out.println("Login successful for: " + req.getEmail());
            System.out.println("=== LOGIN REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== LOGIN REQUEST FAILED ===");
            System.err.println("Error for email: " + req.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Login failed: " + e.getMessage(),
                            "success", false));
        }
    }

    // Existing Refresh Token endpoint
    @Operation(summary = "Refresh Token",
            description = "Generate new access token using refresh token")
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
}