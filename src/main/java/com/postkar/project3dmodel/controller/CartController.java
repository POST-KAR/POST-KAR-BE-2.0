package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.AddToCartRequest;
import com.postkar.project3dmodel.dto.UpdateCartRequest;
import com.postkar.project3dmodel.entity.Cart;
import com.postkar.project3dmodel.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "Shopping cart management APIs")
public class CartController {
    
    private final CartService cartService;
    
    @Operation(
            summary = "Get User Cart",
            description = "Get the current user's shopping cart with all items, quantities, and total amount."
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(Authentication auth) {
        String userId = auth.getName(); // Mobile number from JWT
        log.info("Getting cart for user: {}", userId);
        
        try {
            Cart cart = cartService.getCartByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", cart);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cart for user: {}", userId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get cart");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Add Item to Cart",
            description = "Add a product (marker) to the shopping cart with specified quantity."
    )
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Adding to cart - User: {}, Marker: {}, Quantity: {}", 
                userId, request.getMarkerId(), request.getQuantity());
        
        try {
            Cart cart = cartService.addToCart(userId, request.getMarkerId(), request.getQuantity());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item added to cart successfully");
            response.put("data", cart);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error adding to cart: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error adding to cart", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add item to cart");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Update Cart Item Quantity",
            description = "Update the quantity of a specific item in the cart."
    )
    @PutMapping("/items/{markerId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @Parameter(description = "Marker ID (Product ID)", example = "A01")
            @PathVariable String markerId,
            @Valid @RequestBody UpdateCartRequest request,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Updating cart item - User: {}, Marker: {}, Quantity: {}", 
                userId, markerId, request.getQuantity());
        
        try {
            Cart cart = cartService.updateCartItem(userId, markerId, request.getQuantity());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart item updated successfully");
            response.put("data", cart);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating cart item: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error updating cart item", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update cart item");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Remove Item from Cart",
            description = "Remove a specific item from the shopping cart."
    )
    @DeleteMapping("/items/{markerId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @Parameter(description = "Marker ID (Product ID)", example = "A01")
            @PathVariable String markerId,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Removing from cart - User: {}, Marker: {}", userId, markerId);
        
        try {
            Cart cart = cartService.removeFromCart(userId, markerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item removed from cart successfully");
            response.put("data", cart);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing from cart", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to remove item from cart");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Clear Cart",
            description = "Remove all items from the shopping cart."
    )
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(Authentication auth) {
        String userId = auth.getName();
        log.info("Clearing cart for user: {}", userId);
        
        try {
            cartService.clearCart(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cart cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cart", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to clear cart");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
