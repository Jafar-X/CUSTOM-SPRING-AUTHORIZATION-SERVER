package com.example.spring.authorizationserver.config;


import com.example.spring.authorizationserver.client.service.JpaRegisteredClientRepositoryService;
import com.example.spring.authorizationserver.jose.Jwks;
import com.example.spring.authorizationserver.service.JpaOAuth2AuthorizationService;
import com.example.spring.authorizationserver.user.service.JpaUserDetailsService;
import com.example.spring.authorizationserver.user.service.OidcUserInfoService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.function.Function;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class AuthServerConfig {

    private final JpaRegisteredClientRepositoryService registeredClientRepository;
    private final JpaUserDetailsService userDetailsService;
    private final OidcUserInfoService oidcUserInfoService;
    //private final JpaOAuth2AuthorizationService authorizationService;

    public AuthServerConfig(
            JpaRegisteredClientRepositoryService jpaRegisteredClientRepositoryService,
            JpaUserDetailsService userDetailsService,
            OidcUserInfoService oidcUserInfoService
            /*JpaOAuth2AuthorizationService authorizationService*/) {
        this.registeredClientRepository = jpaRegisteredClientRepositoryService;
        this.userDetailsService = userDetailsService;
        this.oidcUserInfoService = oidcUserInfoService;
        //this.authorizationService = authorizationService;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
            OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
            return oidcUserInfoService.loadUser(authentication.getName());
        };

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        authorizationServerConfigurer.oidc(
                o -> o
                        .providerConfigurationEndpoint(Customizer.withDefaults())
                        .clientRegistrationEndpoint(Customizer.withDefaults())
                        .userInfoEndpoint((userInfo) -> userInfo
                                .userInfoMapper(userInfoMapper)
                        )
        );

        http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(
                        (authorize) -> authorize.anyRequest().authenticated()
                )
                .csrf(
                        (csrf) -> csrf.ignoringRequestMatchers(endpointsMatcher)
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .opaqueToken(Customizer.withDefaults()))
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .with(authorizationServerConfigurer, withDefaults());

        return http.build();
    }

    @Order(2)
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .formLogin(withDefaults());
        return http.build();
    }

    /*@Bean
    public RegisteredClientRepository registeredClientRepository() {
        return registeredClientRepository;
    }
*/
    /*@Bean
    public OAuth2AuthorizationService authorizationService() {
        return authorizationService;
    }*/

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
}