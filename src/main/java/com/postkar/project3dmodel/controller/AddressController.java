package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Address;
import com.postkar.project3dmodel.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Addresses", description = "Address management APIs")
public class AddressController {
    
    private final AddressService addressService;
    
    @Operation(
            summary = "Get User Addresses",
            description = "Get all saved addresses for the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserAddresses(Authentication auth) {
        String userId = auth.getName();
        log.info("Getting addresses for user: {}", userId);
        
        try {
            List<Address> addresses = addressService.getUserAddresses(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", addresses);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting addresses", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get addresses");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Create Address",
            description = "Create a new address for the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAddress(
            @Valid @RequestBody Address address,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Creating address for user: {}", userId);
        
        try {
            Address createdAddress = addressService.createAddress(userId, address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdAddress);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating address", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create address");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Update Address",
            description = "Update an existing address"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{addressId}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @Parameter(description = "Address ID")
            @PathVariable String addressId,
            @Valid @RequestBody Address addressUpdates,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Updating address: {} for user: {}", addressId, userId);
        
        try {
            Address updatedAddress = addressService.updateAddress(addressId, userId, addressUpdates);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedAddress);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating address", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update address");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Delete Address",
            description = "Delete an address"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @Parameter(description = "Address ID")
            @PathVariable String addressId,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Deleting address: {} for user: {}", addressId, userId);
        
        try {
            addressService.deleteAddress(addressId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Address deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting address", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete address");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
