package com.postkar.project3dmodel.security;

import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Custom OAuth2 User Service that handles user creation and updates
 * during OAuth2 authentication flow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String providerId = oauth2User.getAttribute("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        log.info("OAuth2 login attempt - Provider: {}, Email: {}", provider, email);

        // Check if user exists by provider and providerId first
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // If not found by provider, check by email
                    return userRepository.findByEmail(email)
                            .orElseGet(() -> createNewOAuth2User(email, providerId, provider));
                });

        // Update user's OAuth information if needed
        if (user.getProvider() == null || !user.getProvider().equals(provider)) {
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEmailVerified(true);
            user.setActive(true);
            user.setRegistrationStatus(User.RegistrationStatus.COMPLETED);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Updated existing user with OAuth2 information: {}", email);
        }

        return new CustomOAuth2User(oauth2User);
    }

    private User createNewOAuth2User(String email, String providerId, String provider) {
        log.info("Creating new OAuth2 user: {}", email);

        User user = new User();
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setActive(true);
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setRegistrationStatus(User.RegistrationStatus.COMPLETED);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
