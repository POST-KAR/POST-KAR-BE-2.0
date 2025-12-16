package com.postkar.project3dmodel.security;

import com.postkar.project3dmodel.entity.User;
import com.postkar.project3dmodel.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String defaultFrontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.warn("Response already committed. Cannot redirect.");
            return;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomOAuth2User oauth2User)) {
            throw new IllegalStateException("Authentication principal is not CustomOAuth2User");
        }

        String email = oauth2User.getEmail();
        log.info("OAuth2 authentication successful for user: {}", email);

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // Persist tokens
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found after OAuth2 login: " + email));

        user.setJwtToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("JWT tokens generated & saved for user: {}", email);

        // Determine frontend URL dynamically
        String frontendUrl = determineFrontendUrl(request);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth2/redirect")
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        clearAuthenticationAttributes(request);

        log.info("Redirecting OAuth2 user to frontend: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Determine frontend URL using request headers.
     */
    private String determineFrontendUrl(HttpServletRequest request) {

        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            log.debug("Frontend URL detected from Origin header: {}", origin);
            return origin;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = new URI(referer);
                String frontendUrl = uri.getScheme() + "://" + uri.getAuthority();
                log.debug("Frontend URL detected from Referer header: {}", frontendUrl);
                return frontendUrl;
            } catch (Exception ex) {
                log.warn("Invalid Referer header: {}", referer);
            }
        }

        log.debug("Using default frontend URL: {}", defaultFrontendUrl);
        return defaultFrontendUrl;
    }
}
