package com.antoniofaccioli.fishing.ecommerce_backend.support;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

@Configuration
public class KeycloakIdentityProvider {


    Keycloak keycloak;

    @Value("${server-url}")
    private String serverUrl;

    @Value("${admin-realm}")
    private String adminRealm;

    @Value("${client-id}")
    private String clientId ;

    @Value("${grant-type}")
    private String grantType;

    @Value("${name}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${clientSecret}")
    private String clientSecret;

    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityProvider.class);


    @Bean
    public Keycloak keycloakAdminClient() {
        var builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm) // Login admin
                .clientId(clientId)
                .grantType(grantType)
                .username(username)
                .password(password);

        if(clientSecret != null && !clientSecret.isBlank())
            builder.clientSecret(clientSecret);

        return builder.build();
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String name = jwt.getClaimAsString("preferred_username");
            logger.debug("getCurrentUsername: {}", name);
            return name != null ? name : "";
        }
        return "";
    }

    public static List<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            List<String> roles = jwtAuth.getAuthorities()
                    .stream()
                    .map(a -> a.getAuthority())
                    .toList();
            logger.debug("getCurrentRoles: {}", roles);
            return roles;
        }
        return List.of();
    }
}

