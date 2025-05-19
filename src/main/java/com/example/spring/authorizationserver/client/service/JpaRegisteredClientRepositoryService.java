package com.example.spring.authorizationserver.client.service;

import com.example.spring.authorizationserver.client.model.Client;
import com.example.spring.authorizationserver.client.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JpaRegisteredClientRepositoryService implements RegisteredClientRepository {

    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    public JpaRegisteredClientRepositoryService(ClientRepository clientRepository, ObjectMapper objectMapper) {
        this.clientRepository = clientRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = toEntity(registeredClient);
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(id)
                .map(this::toObject)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toObject)
                .orElse(null);
    }

    private RegisteredClient toObject(Client client) {
        Set<String> clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
                client.getClientAuthenticationMethods());
        Set<String> authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
                client.getAuthorizationGrantTypes());
        Set<String> redirectUris = StringUtils.commaDelimitedListToSet(
                client.getRedirectUris());
        Set<String> postLogoutRedirectUris = StringUtils.commaDelimitedListToSet(
                client.getPostLogoutRedirectUris());
        Set<String> clientScopes = StringUtils.commaDelimitedListToSet(
                client.getScopes());

        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId())
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .clientIdIssuedAt(client.getClientIdIssuedAt())
                .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                .clientSecret(client.getClientSecret());

        if (clientAuthenticationMethods != null) {
            clientAuthenticationMethods.forEach(authenticationMethod ->
                    builder.clientAuthenticationMethod(new ClientAuthenticationMethod(authenticationMethod)));
        }

        if (authorizationGrantTypes != null) {
            authorizationGrantTypes.forEach(grantType ->
                    builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
        }

        if (redirectUris != null) {
            redirectUris.forEach(builder::redirectUri);
        }

        if (postLogoutRedirectUris != null) {
            postLogoutRedirectUris.forEach(builder::postLogoutRedirectUri);
        }

        if (clientScopes != null) {
            clientScopes.forEach(builder::scope);
        }

        try {
            Map<String, Object> clientSettingsMap = parseMap(client.getClientSettings());
            builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

            Map<String, Object> tokenSettingsMap = parseMap(client.getTokenSettings());
            builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing client settings", e);
        }

        return builder.build();
    }

    private Client toEntity(RegisteredClient registeredClient) {
        List<String> clientAuthenticationMethods = new ArrayList<>();
        registeredClient.getClientAuthenticationMethods().forEach(method ->
                clientAuthenticationMethods.add(method.getValue()));

        List<String> authorizationGrantTypes = new ArrayList<>();
        registeredClient.getAuthorizationGrantTypes().forEach(grantType ->
                authorizationGrantTypes.add(grantType.getValue()));

        Client client = new Client();
        client.setId(registeredClient.getId());
        client.setClientId(registeredClient.getClientId());
        client.setClientIdIssuedAt(registeredClient.getClientIdIssuedAt());
        client.setClientSecret(registeredClient.getClientSecret());
        client.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        client.setClientName(registeredClient.getClientName());
        client.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
        client.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
        client.setRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getRedirectUris()));
        client.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(registeredClient.getPostLogoutRedirectUris()));
        client.setScopes(StringUtils.collectionToCommaDelimitedString(registeredClient.getScopes()));

        try {
            client.setClientSettings(writeMap(registeredClient.getClientSettings().getSettings()));
            client.setTokenSettings(writeMap(registeredClient.getTokenSettings().getSettings()));
        } catch (Exception e) {
            throw new RuntimeException("Error writing client settings", e);
        }

        return client;
    }

    private Map<String, Object> parseMap(String data) throws JsonProcessingException {
        return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
    }

    private String writeMap(Map<String, Object> data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}
