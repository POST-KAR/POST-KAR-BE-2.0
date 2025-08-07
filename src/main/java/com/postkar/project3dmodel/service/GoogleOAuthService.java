package com.postkar.project3dmodel.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import com.postkar.project3dmodel.security.JwtTokenProvider;

@Service
public class GoogleOAuthService implements OAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Map<String, String> processOAuthUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Email not provided by OAuth provider");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name != null ? name : "Unknown");
                    newUser.setEmailVerified(true);
                    newUser.setProvider("GOOGLE");
                    return userRepository.save(newUser);
                });

        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
}