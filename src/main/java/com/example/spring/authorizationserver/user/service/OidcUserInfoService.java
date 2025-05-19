package com.example.spring.authorizationserver.user.service;


import com.example.spring.authorizationserver.user.model.User;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OidcUserInfoService {

    private final JpaUserDetailsService userDetailsService;

    public OidcUserInfoService(JpaUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public OidcUserInfo loadUser(String username) {
        User user = userDetailsService.findUserByUsername(username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", user.getUsername());
        claims.put("name", user.getFirstName() + " " + user.getLastName());
        claims.put("given_name", user.getFirstName());
        claims.put("family_name", user.getLastName());
        claims.put("email", user.getEmail());
        claims.put("email_verified", true);  // You might want to add email verification logic

        return new OidcUserInfo(claims);
    }
}
