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

    @Operation(summary = "Send OTP to Mobile Number",
            description = "Send verification OTP to the provided mobile number for registration or login")
    @PostMapping("/send-otp")
    public ResponseEntity<RegistrationResponse> sendOtpToMobile(@Valid @RequestBody MobileRequest req) {
        try {
            System.out.println("=== SEND OTP REQUEST STARTED ===");
            System.out.println("Mobile Number: " + req.getMobileNumber());

            RegistrationResponse response = authService.sendOtpToMobile(req);

            System.out.println("OTP sent successfully to: " + req.getMobileNumber());
            System.out.println("=== SEND OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== SEND OTP REQUEST FAILED ===");
            System.err.println("Error for mobile: " + req.getMobileNumber() + " - " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to send OTP: " + e.getMessage(),
                            req.getMobileNumber(), false));
        }
    }

    @Operation(summary = "Verify OTP and Login/Register",
            description = "Verify the OTP sent to the mobile number. Completes registration for new users or logs in existing users.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody MobileOTPVerificationRequest req) {
        try {
            System.out.println("=== VERIFY OTP REQUEST STARTED ===");
            System.out.println("Mobile: " + req.getMobileNumber() + ", OTP: " + req.getOtp());

            Map<String, Object> response = authService.verifyOTP(req);

            System.out.println("OTP verified successfully for: " + req.getMobileNumber());
            System.out.println("=== VERIFY OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== VERIFY OTP REQUEST FAILED ===");
            System.err.println("Error for mobile: " + req.getMobileNumber() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "OTP verification failed: " + e.getMessage(),
                            "success", false));
        }
    }

    @Operation(summary = "Resend OTP",
            description = "Resend verification OTP to the mobile number")
    @PostMapping("/resend-otp")
    public ResponseEntity<RegistrationResponse> resendOtp(@Valid @RequestBody MobileRequest req) {
        try {
            System.out.println("=== RESEND OTP REQUEST STARTED ===");
            System.out.println("Mobile: " + req.getMobileNumber());

            RegistrationResponse response = authService.resendOtp(req);

            System.out.println("OTP resent successfully to: " + req.getMobileNumber());
            System.out.println("=== RESEND OTP REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== RESEND OTP REQUEST FAILED ===");
            System.err.println("Error for mobile: " + req.getMobileNumber() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegistrationResponse("Failed to resend OTP: " + e.getMessage(),
                            req.getMobileNumber(), false));
        }
    }


    @Operation(summary = "Refresh Token",
            description = "Generate new access token using refresh token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            System.out.println("=== REFRESH TOKEN REQUEST STARTED ===");
            System.out.println("Mobile: " + request.getEmail()); // Note: using email field for mobile number

            String newAccessToken = authService.refreshToken(request);
            Map<String, String> response = Map.of("accessToken", newAccessToken);

            System.out.println("Token refreshed successfully for: " + request.getEmail());
            System.out.println("=== REFRESH TOKEN REQUEST SUCCESSFUL ===");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== REFRESH TOKEN REQUEST FAILED ===");
            System.err.println("Error for mobile: " + request.getEmail() + " - " + e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token refresh failed: " + e.getMessage(),
                            "success", false));
        }
    }
}