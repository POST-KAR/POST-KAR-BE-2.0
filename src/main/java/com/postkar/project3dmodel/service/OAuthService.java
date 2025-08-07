package com.postkar.project3dmodel.service;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public interface OAuthService {
    Map<String, String> processOAuthUser(OAuth2User oAuth2User);
}

