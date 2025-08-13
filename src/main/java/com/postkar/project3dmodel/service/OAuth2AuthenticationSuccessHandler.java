package com.postkar.project3dmodel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postkar.project3dmodel.response.OAuthErrorResponse;
import com.postkar.project3dmodel.response.OAuthSuccessResponse;
import com.postkar.project3dmodel.service.OAuthServiceFactory;
import com.postkar.project3dmodel.service.OAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private OAuthServiceFactory oAuthServiceFactory;

    @Autowired
    private OAuthSuccessResponse oAuthSuccessResponse;

    @Autowired
    private OAuthErrorResponse oAuthErrorResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            OAuthService oAuthService = oAuthServiceFactory.getOAuthService(oAuth2User);

            Map<String, String> tokens = oAuthService.processOAuthUser(oAuth2User);

            OAuthSuccessResponse successResponse = new OAuthSuccessResponse();
            successResponse.setSuccess(true);
            successResponse.setMessage("OAuth login successful");
            successResponse.setAccessToken(tokens.get("accessToken"));
            successResponse.setRefreshToken(tokens.get("refreshToken"));
            successResponse.setTokenType("Bearer");

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(objectMapper.writeValueAsString(successResponse));
            response.getWriter().flush();

        } catch (Exception e) {
            handleAuthenticationFailure(response, e);
        }
    }

    private void handleAuthenticationFailure(HttpServletResponse response, Exception e) throws IOException {
        OAuthErrorResponse errorResponse = new OAuthErrorResponse();
        errorResponse.setSuccess(false);
        errorResponse.setError("oauth_error");
        errorResponse.setMessage("OAuth authentication failed: " + e.getMessage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
