package com.antoniofaccioli.fishing.ecommerce_backend.controllers;

import com.antoniofaccioli.fishing.ecommerce_backend.configurations.KeycloakConfig;
import com.antoniofaccioli.fishing.ecommerce_backend.entities.User;
import com.antoniofaccioli.fishing.ecommerce_backend.services.KeycloakService;
import com.antoniofaccioli.fishing.ecommerce_backend.support.domain.HttpResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class KeycloakController {

    record UserDto(String id, String firstName, String lastName, String email) {}

    @Autowired
    KeycloakConfig keycloakUtil;

    @Autowired
    KeycloakService keycloakService;

    @Value("${client-id}")
    private String clientId;

    @Value("${app-realm}")
    private String appRealm;

    //CREATE
    @PostMapping("/auth/signup")
    public ResponseEntity<HttpResponse> registerUser(@Valid @RequestBody UserRequest request) {

        User savedUser = keycloakService.registerNewUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstname(),
                request.getLastname()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                HttpResponse.builder()
                        .timeStamp(String.valueOf(System.currentTimeMillis()))
                        .data(Map.of("user", savedUser))
                        .message("Registrazione completata con successo sia su Keycloak che sul Database locale.")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build()
        );
    }

    // READ
    @GetMapping("/keycloak/currentUser")
    public String getUserId() {
        return keycloakService.getCurrentUserId();
    }

    @GetMapping("/keycloak/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers(){
        Keycloak keycloak = keycloakUtil.getKeycloakInstance();
        List<UserRepresentation> userRepresentations=
                keycloak.realm(appRealm).users().list();
        return keycloakService.mapUsers(userRepresentations);
    }

    // DELETE
    @DeleteMapping("/keycloak/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserAccount(@RequestParam String userId){
        keycloakService.deleteUserAccount(userId);
        return new ResponseEntity<>("Account eliminato!", HttpStatus.ACCEPTED);
    }

    //UPDATE
    @PutMapping("/keycloak/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserAccount(@PathVariable String userId, @Valid @RequestBody UserRequest request){
        log.info("Updated firstname:{}  and lastname:{} of id:{}", request.getFirstname(),request.getLastname(), userId);
        UserRepresentation userRep = keycloakService.updateUserAccount(userId, request.getFirstname(), request.getLastname());
        UserDto dto = new UserDto(userRep.getId(), userRep.getFirstName(), userRep.getLastName(), userRep.getEmail());
        return ResponseEntity.ok(dto);
    }

}

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class UserRequest {
    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
}
