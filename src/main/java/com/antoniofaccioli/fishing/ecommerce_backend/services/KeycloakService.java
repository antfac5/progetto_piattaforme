package com.antoniofaccioli.fishing.ecommerce_backend.services;

import com.antoniofaccioli.fishing.ecommerce_backend.configurations.KeycloakConfig;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import com.antoniofaccioli.fishing.ecommerce_backend.repositories.UserRepository;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
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
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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

    /** Registra un nuovo utente garantendo la consistenza atomica tra Keycloak e il DB locale.
     *  Il metodo è sincronizzato per prevenire race condition concorrenti sullo stesso nodo applicativo.*/
    public synchronized User registerNewUser(String username, String email, String password, String firstName, String lastName) {
        // Validazione input e normalizzazione
        if (username == null || email == null || password == null) {
            throw new CustomException("Username, Email e Password sono obbligatori.");
        }

        String cleanEmail = email.trim().toLowerCase();
        String cleanUsername = username.trim().toLowerCase();

        // Controllo preventivo sul DB Locale (Fast-Fail per alleggerire Keycloak)
        if (userRepository.existsUserByEmail(cleanEmail)) throw new CustomException("Questa email e' già registrata nel sistema.");
        if (userRepository.existsUserByUsername(cleanUsername)) throw new CustomException("Questo username e' già registrato nel sistema.");

        RealmResource realmResource = keycloak.realm(appRealm);
        UsersResource usersResource = realmResource.users();

        // Configurazione credenziali
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        // Configurazione utente Keycloak
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(cleanUsername);
        keycloakUser.setEmail(cleanEmail);
        keycloakUser.setFirstName(firstName);
        keycloakUser.setLastName(lastName);
        keycloakUser.setEnabled(true);
        keycloakUser.setCredentials(List.of(credential));

        // Invio della richiesta a Keycloak
        Response response = null;
        String keycloakUserId = null;

        try {
            response = usersResource.create(keycloakUser);
            if (response.getStatus() == 409) throw new CustomException("Username o Email già esistenti su Keycloak."); // Conflitto nativo rilevato da Keycloak
            if (response.getStatus() != 201) throw new CustomException("Errore Keycloak durante la registrazione. Status: " + response.getStatus());

            // Estrazione ID assegnato da Keycloak
            String path = response.getLocation().getPath();
            keycloakUserId = path.substring(path.lastIndexOf('/') + 1);

            // Assegnazione del Ruolo USER su Keycloak
            List<ClientRepresentation> clients = realmResource.clients().findByClientId("fishing-rest-api");
            if (!clients.isEmpty()) {
                String clientUuid = clients.get(0).getId();
                RoleRepresentation userRole = realmResource.clients().get(clientUuid).roles().get("USER").toRepresentation();
                usersResource.get(keycloakUserId).roles().clientLevel(clientUuid).add(List.of(userRole));
            }

            // Salvataggio su DB Locale con gestione vincoli di integrità
            User localUser = new User();
            localUser.setId(keycloakUserId);
            localUser.setEmail(cleanEmail);
            localUser.setFirstName(firstName);
            localUser.setLastName(lastName);
            localUser.setUsername(cleanUsername);
            localUser.setRoles(Set.of("USER"));

            // se c'è una race condition sull'email o sull'ID, l'eccezione viene lanciata QUI immediatamente e non alla fine del thread/metodo.
            User savedUser = userRepository.saveAndFlush(localUser); // per forzare subito la scrittura sul database.
            log.info("Utente registrato con successo. Keycloak UUID: {}", keycloakUserId);
            return savedUser;

        } catch (DataIntegrityViolationException ex) {
            // La race condition ha superato il controllo iniziale ma il Database locale ha bloccato il duplicato
            log.error("Race condition rilevata sul DB locale per l'email: {}. Avvio compensazione.", cleanEmail);
            cleanUpKeycloakUser(usersResource, keycloakUserId);
            throw new CustomException("Errore di registrazione: i dati inseriti sono già utilizzati da un altro utente.");

        } catch (Exception ex) {
            log.error("Errore imprevisto durante la registrazione dell'utente. Avvio compensazione.", ex);
            cleanUpKeycloakUser(usersResource, keycloakUserId);
            if (ex instanceof CustomException) throw ex;
            throw new CustomException("Impossibile completare la registrazione a causa di un errore interno.");
        } finally {
            if (response != null) response.close(); // Cruciale per evitare leak di connessioni HTTP
        }
    }

    /**
     * Metodo ausiliario di compensazione (Rollback manuale di Keycloak)
     */
    private void cleanUpKeycloakUser(UsersResource usersResource, String keycloakUserId) {
        if (keycloakUserId != null) {
            try {
                usersResource.get(keycloakUserId).remove();
                log.warn("Compensazione eseguita: rimosso utente orfano {} da Keycloak.", keycloakUserId);
            } catch (WebApplicationException wae) {
                log.error("Impossibile rimuovere l'utente orfano da Keycloak. ID: {}. Status: {}", keycloakUserId, wae.getResponse().getStatus());
            } catch (Exception e) {
                log.error("Errore durante la rimozione dell'utente orfano da Keycloak. ID: {}", keycloakUserId, e);
            }
        }
    }

    /** Metodo che restituisce l'id dell'utente attualmente autenticato, estraendolo dal token JWT presente nel contesto
     di sicurezza di Spring Security. Se non c'è un utente autenticato o se il token non è un JWT valido, viene sollevata un'eccezione.*/
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
        user.setUsername(userRep.getUsername());
        user.setRoles(extractRoles(userRep));
        return user;
    }

    /** Metodo che estrae la data di creazione dell'utente a partire dal timestamp presente nella rappresentazione
     * utente (UserRepresentation) restituita da Keycloak. Il timestamp viene convertito in un oggetto LocalDateTime
     * utilizzando il fuso orario di sistema.*/
    private LocalDateTime extractCreatedAt(UserRepresentation userRepresentation) {
        long timestamp = userRepresentation.getCreatedTimestamp();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /** Metodo che mappa un oggetto User (entità del dominio) a un oggetto UserRepresentation
    * (rappresentazione dell'utente utilizzata da Keycloak).*/
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
