package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.CheckoutRequest;
import com.postkar.project3dmodel.entity.Cart;
import com.postkar.project3dmodel.entity.Order;
import com.postkar.project3dmodel.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    
    @Transactional
    public Order createOrderFromCart(String userId, CheckoutRequest request) {
        log.info("Creating order for user: {}", userId);
        
        // Get user's cart
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }
        
        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        
        // Convert cart items to order items
        List<Order.OrderItem> orderItems = cart.getItems().stream()
                .map(this::convertCartItemToOrderItem)
                .collect(Collectors.toList());
        order.setItems(orderItems);
        
        order.setTotalAmount(cart.getTotalAmount());
        order.setCurrency(cart.getCurrency());
        order.setStatus(Order.OrderStatus.PENDING);
        
        // Set addresses
        order.setShippingAddress(convertToShippingAddress(request.getShippingAddress()));
        if (request.getBillingAddress() != null) {
            order.setBillingAddress(convertToShippingAddress(request.getBillingAddress()));
        } else {
            order.setBillingAddress(order.getShippingAddress()); // Use shipping as billing
        }
        
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after successful order creation
        cartService.clearCart(userId);
        
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return savedOrder;
    }
    
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Page<Order> getOrdersByUserId(String userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Optional<Order> getOrderById(String orderId, String userId) {
        return orderRepository.findByIdAndUserId(orderId, userId);
    }
    
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    @Transactional
    public Order updateOrderStatus(String orderId, Order.OrderStatus status) {
        log.info("Updating order status - Order: {}, Status: {}", orderId, status);
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        // Generate order number: ORD-YYYYMMDD-HHMMSS-XXX
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return "ORD-" + timestamp + "-" + random;
    }
    
    private Order.OrderItem convertCartItemToOrderItem(Cart.CartItem cartItem) {
        Order.OrderItem orderItem = new Order.OrderItem();
        orderItem.setMarkerId(cartItem.getMarkerId());
        orderItem.setMarkerName(cartItem.getMarkerName());
        orderItem.setThumbnailUrl(cartItem.getThumbnailUrl());
        orderItem.setUnitPrice(cartItem.getUnitPrice());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setTotalPrice(cartItem.getTotalPrice());
        orderItem.setCurrency(cartItem.getCurrency());
        return orderItem;
    }
    
    private Order.ShippingAddress convertToShippingAddress(CheckoutRequest.ShippingAddressDto dto) {
        Order.ShippingAddress address = new Order.ShippingAddress();
        address.setFullName(dto.getFullName());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setPhone(dto.getPhone());
        return address;
    }
}
