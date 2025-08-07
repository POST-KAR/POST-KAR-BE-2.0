package com.postkar.project3dmodel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuthServiceFactory {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private FacebookOAuthService facebookOAuthService;

    public OAuthService getOAuthService(OAuth2User oAuth2User) {
        if (oAuth2User.getAttributes().containsKey("sub")) {
            return googleOAuthService;
        } else if (oAuth2User.getAttributes().containsKey("id") &&
                !oAuth2User.getAttributes().containsKey("sub")) {
            return facebookOAuthService;
        } else {
            throw new RuntimeException("Unsupported OAuth provider");
        }
    }

    public OAuthService getOAuthServiceByProvider(String providerName) {
        switch (providerName.toLowerCase()) {
            case "google":
                return googleOAuthService;
            case "facebook":
                return facebookOAuthService;
            default:
                throw new RuntimeException("Unsupported OAuth provider: " + providerName);
        }
    }
}
