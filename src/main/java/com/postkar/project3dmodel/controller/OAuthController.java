package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.service.OAuthService;
import com.postkar.project3dmodel.service.OAuthServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@Tag(name = "OAuth2 Login", description = "OAuth2 login APIs for Google and Facebook authentication")
public class OAuthController {

    @Autowired
    private OAuthServiceFactory oAuthServiceFactory;

    @Operation(
            summary = "Handle OAuth2 Login Success",
            description = "Processes a successful OAuth2 authentication for Google or Facebook and returns user details or tokens."
    )
    @GetMapping("/success")
    public Map<String, String> handleOAuthSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            throw new RuntimeException("OAuth2 user not authenticated");
        }
        OAuthService oAuthService = oAuthServiceFactory.getOAuthService(oAuth2User);
        return oAuthService.processOAuthUser(oAuth2User);
    }
}