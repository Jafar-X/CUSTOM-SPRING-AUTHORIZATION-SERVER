package com.example.spring.authorizationserver.config;

import com.example.spring.authorizationserver.client.repository.AuthorizationRepository;
import com.example.spring.authorizationserver.service.JpaOAuth2AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration
public class AuthorizationServiceConfig {
    @Bean
    public JpaOAuth2AuthorizationService jpaOAuth2AuthorizationService(
            AuthorizationRepository authorizationRepository,
            RegisteredClientRepository registeredClientRepository) {
        return new JpaOAuth2AuthorizationService(authorizationRepository, registeredClientRepository);
    }
}
