package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @GetMapping("/success")
    public Map<String, String> handleOAuthSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        return googleOAuthService.processOAuthUser(oAuth2User);
    }
}