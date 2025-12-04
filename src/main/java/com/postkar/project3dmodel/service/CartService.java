package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.Cart;
import com.postkar.project3dmodel.entity.Marker;
import com.postkar.project3dmodel.repository.CartRepository;
import com.postkar.project3dmodel.repository.MarkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final MarkerRepository markerRepository;
    private final SignedUrlService signedUrlService;
    
    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
    }
    
    @Transactional
    public Cart addToCart(String userId, String markerId, Integer quantity) {
        log.info("Adding to cart - User: {}, Marker: {}, Quantity: {}", userId, markerId, quantity);
        
        // Get or create cart
        Cart cart = getCartByUserId(userId);
        
        // Get marker details
        Optional<Marker> markerOpt = markerRepository.findByMarkerId(markerId);
        if (markerOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + markerId);
        }
        
        Marker marker = markerOpt.get();
        if (!marker.isActive()) {
            throw new IllegalArgumentException("Product is not available: " + markerId);
        }
        
        if (marker.getPrice() == null) {
            throw new IllegalArgumentException("Product price not set: " + markerId);
        }
        
        // Check stock
        if (!marker.getInStock() || marker.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + markerId);
        }
        
        // Check if item already exists in cart
        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMarkerId().equals(markerId))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item
            Cart.CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.calculateTotalPrice();
        } else {
            // Add new item
            Cart.CartItem newItem = new Cart.CartItem();
            newItem.setMarkerId(markerId);
            newItem.setMarkerName(marker.getName());
            newItem.setThumbnailUrl(signedUrlService.generateSignedUrl(marker.getThumbnailUrl()));
            newItem.setUnitPrice(marker.getPrice());
            newItem.setQuantity(quantity);
            newItem.setCurrency(marker.getCurrency());
            newItem.calculateTotalPrice();
            
            cart.getItems().add(newItem);
        }
        
        cart.calculateTotalAmount();
        cart.setUpdatedAt(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart updateCartItem(String userId, String markerId, Integer quantity) {
        log.info("Updating cart item - User: {}, Marker: {}, Quantity: {}", userId, markerId, quantity);
        
        Cart cart = getCartByUserId(userId);
        
        Optional<Cart.CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getMarkerId().equals(markerId))
                .findFirst();
        
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found in cart: " + markerId);
        }
        
        Cart.CartItem item = itemOpt.get();
        item.setQuantity(quantity);
        item.calculateTotalPrice();
        
        cart.calculateTotalAmount();
        cart.setUpdatedAt(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart removeFromCart(String userId, String markerId) {
        log.info("Removing from cart - User: {}, Marker: {}", userId, markerId);
        
        Cart cart = getCartByUserId(userId);
        
        cart.getItems().removeIf(item -> item.getMarkerId().equals(markerId));
        cart.calculateTotalAmount();
        cart.setUpdatedAt(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartRepository.deleteByUserId(userId);
    }
    
    private Cart createEmptyCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }
}
