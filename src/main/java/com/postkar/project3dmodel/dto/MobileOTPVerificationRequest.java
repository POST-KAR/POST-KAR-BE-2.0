package com.postkar.project3dmodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileOTPVerificationRequest {
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
    private String mobileNumber;
    
    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 6, message = "OTP must be between 4 and 6 digits")
    @Pattern(regexp = "^\\d+$", message = "OTP must contain only digits")
    private String otp;
}
