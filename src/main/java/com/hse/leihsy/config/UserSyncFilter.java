package com.hse.leihsy.config;

import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Filter der bei jedem authentifizierten Request prüft,
 * ob der User bereits in der Datenbank existiert.
 * Falls nicht, wird er automatisch aus den Keycloak-Token-Daten angelegt.
 */
@Component
public class UserSyncFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UserSyncFilter.class);

    private final UserRepository userRepository;

    public UserSyncFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Nur wenn User authentifiziert ist und ein JWT Token hat
            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof Jwt jwt) {

                syncUserFromToken(jwt);
            }
        } catch (Exception e) {
            // Fehler beim User-Sync sollten den Request nicht blockieren
            log.error("Error during user sync: {}", e.getMessage());
        }

        // Request weiterleiten (auch wenn Sync fehlschlägt)
        filterChain.doFilter(request, response);
    }

    /**
     * Synchronisiert User-Daten aus dem JWT Token mit der Datenbank.
     * Erstellt neuen User falls nicht vorhanden, aktualisiert Namen falls geändert.
     */
    private void syncUserFromToken(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        // Prüfen ob User bereits existiert
        userRepository.findByUniqueId(keycloakId).ifPresentOrElse(
                existingUser -> {
                    // User existiert - optional: Namen aktualisieren falls geändert
                    String tokenName = extractName(jwt);
                    if (tokenName != null && !tokenName.equals(existingUser.getName())) {
                        existingUser.setName(tokenName);
                        userRepository.save(existingUser);
                        log.debug("Updated user name for: {}", keycloakId);
                    }
                },
                () -> {
                    // User existiert nicht - neu anlegen
                    createUserFromToken(jwt, keycloakId);
                }
        );
    }

    /**
     * Erstellt einen neuen User aus den JWT Token Claims
     */
    private void createUserFromToken(Jwt jwt, String keycloakId) {
        String name = extractName(jwt);
        String email = jwt.getClaimAsString("email");

        User newUser = new User();
        newUser.setUniqueId(keycloakId);
        newUser.setName(name != null ? name : "Unknown");
        newUser.setBudget(BigDecimal.ZERO);

        userRepository.save(newUser);

        log.info("Created new user from Keycloak: id={}, name={}, email={}",
                keycloakId, name, email);
    }

    /**
     * Extrahiert den Namen aus dem JWT Token.
     * Versucht verschiedene Claims in Prioritätsreihenfolge.
     */
    private String extractName(Jwt jwt) {
        // Priorität: preferred_username > name > given_name + family_name > email
        String name = jwt.getClaimAsString("preferred_username");

        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("name");
        }

        if (name == null || name.isBlank()) {
            String givenName = jwt.getClaimAsString("given_name");
            String familyName = jwt.getClaimAsString("family_name");
            if (givenName != null && familyName != null) {
                name = givenName + " " + familyName;
            }
        }

        if (name == null || name.isBlank()) {
            name = jwt.getClaimAsString("email");
        }

        return name;
    }

    /**
     * Filter nur für API-Requests ausführen, nicht für statische Ressourcen
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Nicht filtern für: Swagger, H2-Console, statische Ressourcen
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.equals("/")
                || path.equals("/login")
                || path.equals("/error");
    }
}