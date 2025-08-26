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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Handle null password - this is crucial for OAuth users or users without passwords
        String password = user.getPassword();
        if (password == null) {
            password = ""; // Empty string for users without passwords
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password) // Now guaranteed to be non-null
                .authorities(Collections.emptyList()) // No roles needed
                .build();
    }
}