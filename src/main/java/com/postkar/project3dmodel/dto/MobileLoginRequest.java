package com.postkar.project3dmodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileLoginRequest {
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid mobile number format")
    private String mobileNumber;
}
