package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.configurations.KeycloakConfig;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.UserRepository;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import com.antoniofaccioli.fishing.ecommerce_backend.support.exceptions.CustomException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j //Annotazione per abilitare il logging con Lombok
public class KeycloakService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    KeycloakConfig keycloak;

    @Value("${app-realm}")
    private String appRealm;

    // Metodo che restituisce l'id dell'utente attualmente autenticato, estraendolo dal token JWT presente nel contesto
    // di sicurezza di Spring Security. Se non c'è un utente autenticato o se il token non è un JWT valido, viene sollevata un'eccezione.
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); //recupera l'oggetto Authentication dal contesto di sicurezza di Spring Security, che rappresenta l'utente attualmente autenticato e le sue credenziali
        if (!(auth instanceof JwtAuthenticationToken jwtAuth) || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated JWT user in security context");
        }
        return jwtAuth.getToken().getSubject(); //estrae l'id dell'utente (subject) dal token JWT presente nell'oggetto JwtAuthenticationToken, che rappresenta l'utente autenticato tramite JWT
    }

    // Metodo che restituisce il contesto di sicurezza di Keycloak, estraendolo dall'oggetto Authentication presente nel
    // contesto di sicurezza di Spring Security. Se non c'è un utente autenticato o se il token non è un JWT valido,
    // viene sollevata un'eccezione.
    private Set<String> extractRoles(UserRepresentation userRepresentation) {
        RealmResource realmResource = keycloak.realm(appRealm);
        UsersResource usersResource = realmResource.users();
        UserResource userResource = usersResource.get(userRepresentation.getId());

        List<ClientRepresentation> clients = realmResource.clients().findByClientId("fishing-rest-api");
        if (clients.isEmpty())
            return Set.of(); //se non viene trovato il client con l'id "fishing-rest-api", restituisce un set vuoto di ruoli
        String clientUuid = clients.get(0).getId();

        List<RoleRepresentation> roles = userResource.roles()
                .clientLevel(clientUuid)
                .listAll();

        return roles.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    public List<User> mapUsers(List<UserRepresentation> userRepresentations) {
        List<User> users = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userRepresentations)) {
            userRepresentations.forEach(userRep -> {
                users.add(mapUser(userRep));
            });
        }
        return users;
    }

    private User mapUser(UserRepresentation userRep) {
        User user = new User();
        user.setId(userRep.getId());
        user.setFirstName(userRep.getFirstName());
        user.setLastName(userRep.getLastName());
        user.setEmail(userRep.getEmail());
        user.setRoles(extractRoles(userRep));
        return user;
    }

    // Metodo che estrae la data di creazione dell'utente a partire dal timestamp presente nella rappresentazione
    // dell'utente (UserRepresentation) restituita da Keycloak. Il timestamp viene convertito in un oggetto LocalDateTime
    // utilizzando il fuso orario di sistema.
    private LocalDateTime extractCreatedAt(UserRepresentation userRepresentation) {
        long timestamp = userRepresentation.getCreatedTimestamp();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /*private static String extractAttribute(UserRepresentation userRepresentation, String property) {
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        if (attributes != null && attributes.size() > 0) {
            List<String> properties = attributes.get(property);
            if (!CollectionUtils.isEmpty(properties)) {
                return properties.get(0);
            }
        }
        return null;
    }*/

    // Metodo che mappa un oggetto User (entità del dominio) a un oggetto UserRepresentation
    // (rappresentazione dell'utente utilizzata da Keycloak).
    private static UserRepresentation mapUserRep(User user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setId(user.getId());
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEmail(user.getEmail());
        return userRep;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<UserRepresentation> getUserById(String userId) {
        try {
            return Optional.ofNullable(keycloak.realm(appRealm).users().get(userId).toRepresentation()); //restituisce un'istanza di UserRepresentation che rappresenta l'utente con l'id specificato, oppure null se l'utente non viene trovato. Il risultato viene avvolto in un oggetto Optional per gestire il caso in cui l'utente non esista.
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserAccount(String userId) {
        //  log.info("Deleting user {} in realm {}", user.getSpec().getUsername(), realm);
        Optional<UserRepresentation> userRepresentation = getUserById(userId);
        if( userRepresentation.isPresent() ){
            RealmResource realmResource = keycloak.realm(appRealm);
            UsersResource usersResource = realmResource.users();
            usersResource.get(userId)
                    .remove();
            userRepository.deleteById(userId);
        }else{
            throw new CustomException("Errore nell'eliminare l'utente.");
        }
    }

    @PreAuthorize("hasRole('ADMIN') or #userId == principal")
    public UserRepresentation updateUserAccount(String userId, String firstName, String lastName) {
        RealmResource realmResource = keycloak.realm(appRealm);

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new CustomException("Nome e cognome obbligatori.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException("Utente non trovato."));

        //aggiornamento keycloak
        UserRepresentation userRep = new UserRepresentation();
        userRep.setFirstName(firstName);
        userRep.setLastName(lastName);
        keycloak.realm(appRealm).users().get(userId).update(userRep);

        //aggiornamento database
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);

        return userRep;
    }
}
