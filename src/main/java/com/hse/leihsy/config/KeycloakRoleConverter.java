package com.hse.leihsy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Konvertiert Keycloak JWT Token Rollen zu Spring Security GrantedAuthorities.
 *
 * Keycloak speichert Rollen an verschiedenen Stellen im Token:
 * - realm_access.roles: Realm-weite Rollen
 * - resource_access.{client-id}.roles: Client-spezifische Rollen
 *
 * Dieser Converter liest beide aus und erstellt ROLE_* Authorities.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_KEY = "roles";

    // Client-ID fuer LeihSy in Keycloak
    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Realm Roles auslesen
        authorities.addAll(extractRealmRoles(jwt));

        // 2. Client Roles auslesen (fuer LeihSy)
        authorities.addAll(extractClientRoles(jwt, clientId));

        return authorities;
    }

    /**
     * Extrahiert Realm-weite Rollen aus dem Token
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);

        if (realmAccess == null || !realmAccess.containsKey(ROLES_KEY)) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get(ROLES_KEY);

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    /**
     * Extrahiert Client-spezifische Rollen aus dem Token
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt, String clientId) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);

        if (resourceAccess == null || !resourceAccess.containsKey(clientId)) {
            return Collections.emptyList();
        }

        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);

        if (clientAccess == null || !clientAccess.containsKey(ROLES_KEY)) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) clientAccess.get(ROLES_KEY);

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}