package com.postkar.project3dmodel.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Custom OAuth2User wrapper that provides easy access to user attributes
 * from Google OAuth2 authentication
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;
    private final String email;
    private final String name;
    private final String picture;

    public CustomOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
        this.email = oauth2User.getAttribute("email");
        this.name = oauth2User.getAttribute("name");
        this.picture = oauth2User.getAttribute("picture");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        return name;
    }
}
