package com.postkar.project3dmodel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(mobileNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + mobileNumber));

        // Since we're using OTP-based authentication, we don't need a password
        // But Spring Security requires one, so we'll use a placeholder
        String password = "N/A";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhoneNumber())
                .password(password)
                .authorities(Collections.emptyList())
                .accountExpired(!user.isActive())
                .accountLocked(!user.isMobileVerified())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}