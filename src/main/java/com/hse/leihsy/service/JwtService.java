package com.hse.leihsy.service;

import com.hse.leihsy.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    /**
     * Holt die Keycloak unique_id aus dem JWT Token
     * @return unique_id (Subject Claim als String)
     */
    public String getCurrentUserUniqueId() {
        Jwt jwt = getCurrentJwt();
        return jwt.getSubject(); //
    }

    /**
     * Holt den Username aus dem JWT Token
     * @return preferred_username
     */
    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("preferred_username");
    }

    /**
     * Holt die Email aus dem JWT Token
     * @return email
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("email");
    }

    /**
     * Prüft ob der aktuelle User die Rolle "admin" hat
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Prüft ob der aktuelle User die Rolle "lender" hat
     */
    public boolean isLender() {
        return hasRole("lender");
    }

    /**
     * Prüft ob eine bestimmte Rolle vorhanden ist
     */
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_" + role.toUpperCase())
                );
    }

    /**
     * Holt das JWT Token aus dem SecurityContext
     */
    private Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            throw new UnauthorizedException("No JWT token found in SecurityContext");
        }
        return (Jwt) authentication.getPrincipal();
    }
}