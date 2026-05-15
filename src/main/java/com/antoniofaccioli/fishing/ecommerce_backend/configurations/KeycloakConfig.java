package com.antoniofaccioli.fishing.ecommerce_backend.configurations;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakConfig {

    private Keycloak keycloak;

    @Value("${server-url}")
    private String serverUrl;

    @Value("${admin-realm}")
    private String adminRealm;

    @Value("${app-realm}")
    private String appRealm;

    @Value("${client-id}")
    private String clientId;

    @Value("${grant-type}")
    private String grantType;

    @Value("${name}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${clientSecret:}")
    private String clientSecret;

    public Keycloak getKeycloakInstance() {
        if (keycloak == null) {
            var builder = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(adminRealm) // Login admin
                    .clientId(clientId)
                    .grantType(grantType)
                    .username(username)
                    .password(password);

            if (clientSecret != null && !clientSecret.isBlank()) {
                builder.clientSecret(clientSecret);
            }

            keycloak = builder.build();
        }
        return keycloak;
    }

    public RealmResource realm(){
        return getKeycloakInstance().realm(appRealm); //Realm applicativo
    }

    public RealmResource realm(String realm) {
        return getKeycloakInstance().realm(realm);
    }
}
