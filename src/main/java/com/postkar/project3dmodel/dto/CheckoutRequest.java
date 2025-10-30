package com.postkar.project3dmodel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;
    
    @NotBlank(message = "Customer phone is required")
    private String customerPhone;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDto shippingAddress;
    
    @Valid
    private ShippingAddressDto billingAddress; // Optional, if null use shipping address
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        
        @NotBlank(message = "Full name is required")
        private String fullName;
        
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;
        
        private String addressLine2;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Postal code is required")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        private String country;
        
        @NotBlank(message = "Phone is required")
        private String phone;
    }
}
