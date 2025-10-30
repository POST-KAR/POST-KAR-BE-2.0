package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.CheckoutRequest;
import com.postkar.project3dmodel.entity.Order;
import com.postkar.project3dmodel.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(
            summary = "Create Order (Checkout)",
            description = "Create a new order from the user's cart items with shipping and billing information."
    )
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @Valid @RequestBody CheckoutRequest request,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Creating order for user: {}", userId);
        
        try {
            Order order = orderService.createOrderFromCart(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("data", order);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error creating order: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error creating order", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create order");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get User Orders",
            description = "Get all orders for the authenticated user, ordered by creation date (newest first)."
    )
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @Parameter(description = "Enable pagination", example = "false")
            @RequestParam(defaultValue = "false") boolean paginated,
            Pageable pageable,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Getting orders for user: {}", userId);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            if (paginated) {
                Page<Order> orders = orderService.getOrdersByUserId(userId, pageable);
                response.put("data", orders.getContent());
                response.put("totalElements", orders.getTotalElements());
                response.put("totalPages", orders.getTotalPages());
                response.put("currentPage", orders.getNumber());
                response.put("pageSize", orders.getSize());
            } else {
                List<Order> orders = orderService.getOrdersByUserId(userId);
                response.put("data", orders);
                response.put("count", orders.size());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting orders for user: {}", userId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get orders");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get Order Details",
            description = "Get detailed information for a specific order by order ID."
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @Parameter(description = "Order ID", example = "64f1b2c3d4e5f6789abcdef0")
            @PathVariable String orderId,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Getting order details - User: {}, Order: {}", userId, orderId);
        
        try {
            return orderService.getOrderById(orderId, userId)
                    .map(order -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("data", order);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Order not found");
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error getting order details", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get order details");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @Operation(
            summary = "Get Order by Order Number",
            description = "Get order details using the human-readable order number."
    )
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByOrderNumber(
            @Parameter(description = "Order Number", example = "ORD-20241027-143022-123")
            @PathVariable String orderNumber,
            Authentication auth
    ) {
        String userId = auth.getName();
        log.info("Getting order by number - User: {}, Order Number: {}", userId, orderNumber);
        
        try {
            return orderService.getOrderByOrderNumber(orderNumber)
                    .filter(order -> order.getUserId().equals(userId)) // Ensure user owns the order
                    .map(order -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("data", order);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("success", false);
                        errorResponse.put("message", "Order not found");
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Error getting order by number", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to get order");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
