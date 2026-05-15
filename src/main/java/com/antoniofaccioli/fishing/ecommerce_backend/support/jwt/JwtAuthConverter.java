package com.antoniofaccioli.fishing.ecommerce_backend.support.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter(); /* GrantedAuthority è un'interfaccia di Spring Security che
                                                     rappresenta un'autorità (permesso/ruolo) concesso a un utente
                                                     autenticato*/

    @Value("${jwt.auth.converter.principal-attribute}")
    private String principalAttribute;

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;


    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt){
        // estrae le authorities dal jwt token e le converte in un set di GrantedAuthority
        Collection<GrantedAuthority> authorities = Stream.concat( //crea uno stream concatenando due stream di authorities
                        jwtGrantedAuthoritiesConverter.convert(jwt).stream(), //estrae le authorities standard dal jwt token (ad esempio, quelle presenti nella claim "scope" o "authorities")
                        extractResourceRoles(jwt).stream() //estrae le authorities specifiche per il client (risorsa) dal jwt token, utilizzando la claim "resource_access" e il resourceId configurato
                )
                .collect(Collectors.toSet());

        return new JwtAuthenticationToken( //crea un'istanza di JwtAuthenticationToken, che è una classe di Spring Security che rappresenta un token di autenticazione basato su JWT
                jwt,
                authorities,
                getPrincipleClaimName(jwt)
        );
    }//convert

    private String getPrincipleClaimName(Jwt jwt) {
        //estrae il prefered_username dal jwt token
        String claimName= (principalAttribute != null && !principalAttribute.isBlank())
                ? principalAttribute
                : JwtClaimNames.SUB;        //default in jwt token
        Object claim = jwt.getClaims().get(claimName);

        if (claim instanceof String){
            String s = (String) claim;
            if (!s.isBlank()) return s;
        }

        return jwt.getSubject();
    }//getPrincipleClaimName

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt){
        Object resourceAccessObj = jwt.getClaims().get("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?>)) {
            return Set.of();
        }
        Map resourceAccess = (Map) resourceAccessObj;
        Object clientIdObj = resourceAccess.get(resourceId);
        if (!(clientIdObj instanceof Map<?, ?>)) {
            return Set.of();
        }
        Map clientId = (Map) clientIdObj;
        Object rolesObj = clientId.get("roles");
        if (!(rolesObj instanceof Collection<?>)) {
            return Set.of();
        }
        Collection<String> roles = (Collection<String>) rolesObj;

        return roles.stream() //crea uno stream a partire dalla collezione di ruoli
                .filter(Objects::nonNull) //filtra i ruoli nulli
                .map(Object::toString)  //converte i ruoli in stringhe (nel caso in cui non siano già stringhe)
                .map(String::trim) //rimuove gli spazi bianchi all'inizio e alla fine dei ruoli
                .filter(role -> !role.isEmpty())    //filtra i ruoli vuoti
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) //aggiunge il prefisso "ROLE_" ai ruoli per conformarsi alla convenzione di Spring Security
                .collect(Collectors.toSet()); //colleziona i ruoli in un set per evitare duplicati
    }//extractResourceRoles
}
