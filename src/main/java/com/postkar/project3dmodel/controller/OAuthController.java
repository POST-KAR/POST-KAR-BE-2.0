package com.postkar.project3dmodel.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.postkar.project3dmodel.service.OAuthService;
import com.postkar.project3dmodel.service.OAuthServiceFactory;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Autowired
    private OAuthServiceFactory oAuthServiceFactory;

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> handleOAuthSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            OAuthService oAuthService = oAuthServiceFactory.getOAuthService(oAuth2User);

            Map<String, String> tokens = oAuthService.processOAuthUser(oAuth2User);

            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            throw new RuntimeException("OAuth processing failed: " + e.getMessage());
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(oAuth2User.getAttributes());
    }
}