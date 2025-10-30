package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    private String userId; // Mobile number
    
    private String orderNumber; // Human-readable order number
    
    private String customerName;
    
    private String customerEmail;
    
    private String customerPhone;
    
    private List<OrderItem> items = new ArrayList<>();
    
    private BigDecimal totalAmount;
    
    private String currency = "INR";
    
    private OrderStatus status;
    
    private ShippingAddress shippingAddress;
    
    private ShippingAddress billingAddress;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime deliveredAt;
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String markerId;
        private String markerName;
        private String thumbnailUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal totalPrice;
        private String currency = "INR";
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String fullName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phone;
    }
}
