package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.Address;
import com.postkar.project3dmodel.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {
    
    private final AddressRepository addressRepository;
    
    public List<Address> getUserAddresses(String userId) {
        log.info("Getting addresses for user: {}", userId);
        return addressRepository.findByUserId(userId);
    }
    
    public Optional<Address> getAddressById(String id, String userId) {
        return addressRepository.findByIdAndUserId(id, userId);
    }
    
    public Address createAddress(String userId, Address address) {
        log.info("Creating address for user: {}", userId);
        address.setUserId(userId);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return addressRepository.save(address);
    }
    
    public Address updateAddress(String id, String userId, Address addressUpdates) {
        log.info("Updating address: {} for user: {}", id, userId);
        
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        if (addressUpdates.getFullName() != null) {
            address.setFullName(addressUpdates.getFullName());
        }
        if (addressUpdates.getPhone() != null) {
            address.setPhone(addressUpdates.getPhone());
        }
        if (addressUpdates.getLine1() != null) {
            address.setLine1(addressUpdates.getLine1());
        }
        if (addressUpdates.getLine2() != null) {
            address.setLine2(addressUpdates.getLine2());
        }
        if (addressUpdates.getCity() != null) {
            address.setCity(addressUpdates.getCity());
        }
        if (addressUpdates.getState() != null) {
            address.setState(addressUpdates.getState());
        }
        if (addressUpdates.getPostalCode() != null) {
            address.setPostalCode(addressUpdates.getPostalCode());
        }
        if (addressUpdates.getCountry() != null) {
            address.setCountry(addressUpdates.getCountry());
        }
        if (addressUpdates.getIsDefault() != null) {
            address.setIsDefault(addressUpdates.getIsDefault());
        }
        
        address.setUpdatedAt(LocalDateTime.now());
        return addressRepository.save(address);
    }
    
    public void deleteAddress(String id, String userId) {
        log.info("Deleting address: {} for user: {}", id, userId);
        
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        addressRepository.delete(address);
    }
}
