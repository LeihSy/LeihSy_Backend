package com.hse.leihsy.service;

import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.exception.UnauthorizedException;

import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll(); 
        }
        return userRepository.findByNameContainingIgnoreCase(query);
    }
    /**
     * Holt oder erstellt einen User basierend auf Keycloak-Daten
     * @param uniqueId Keycloak Subject ID
     * @param name Username aus Keycloak
     * @param email Email aus Keycloak
     * @return User Entity
     */
    @Transactional
    public User getOrCreateUser(String uniqueId, String name, String email) {
        return userRepository.findByUniqueId(uniqueId)
                .orElseGet(() -> createUser(uniqueId, name, email));
    }

    /**
     * Erstellt einen neuen User
     */
    private User createUser(String uniqueId, String name, String email) {
        User user = new User();
        user.setUniqueId(uniqueId);
        user.setName(name);
        user.setBudget(BigDecimal.ZERO); // Später für Nice-to-Have Feature
        User savedUser = userRepository.save(user);

        log.info("Created new user: {} (Keycloak ID: {})", name, maskId(uniqueId));
        return savedUser;
    }

    /**
     * Holt User anhand der Database-ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Holt User anhand der Keycloak unique_id
     */
    public User getUserByUniqueId(String uniqueId) {
        return userRepository.findByUniqueId(uniqueId)
                .orElseThrow(() -> new RuntimeException("User not found with uniqueId: " + uniqueId));
    }

    /**
     * Holt User anhand des Namens
     */
    public User getUserByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with name: " + name));
    }

    /**
     * Holt den aktuell eingeloggten User aus dem JWT Token
     * @return Current User
     */
    public User getCurrentUser() {
        log.debug("getCurrentUser() called");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                log.error("Authentication is NULL - this should never happen!");
                throw new UnauthorizedException("Access Denied: No authentication found");
            }

            log.debug("Authentication found: {}", authentication.getClass().getSimpleName());
            log.debug("Principal type: {}", authentication.getPrincipal().getClass().getSimpleName());

            if (!authentication.isAuthenticated()) {
                log.error("User is not authenticated despite having authentication object");
                throw new UnauthorizedException("Access Denied: User not authenticated");
            }

            if (!(authentication.getPrincipal() instanceof Jwt)) {
                log.error("Principal is not a JWT! Type: {}", authentication.getPrincipal().getClass().getName());
                throw new UnauthorizedException("Access Denied: Invalid authentication type");
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakId = jwt.getSubject();

            log.debug("Processing request for Keycloak ID: {}", maskId(keycloakId));

            User user = userRepository.findByUniqueId(keycloakId)
                    .orElseGet(() -> {
                        log.info("User not found in DB, creating new user for Keycloak ID: {}", maskId(keycloakId));
                        String name = jwt.getClaim("preferred_username");
                        String email = jwt.getClaim("email");
                        return createUser(keycloakId, name, email);
                    });

            log.debug("User {} (ID={}) successfully authenticated", user.getName(), user.getId());

            return user;

        } catch (Exception e) {
            log.error("Exception in getCurrentUser(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Maskiert eine sensitive ID für Logging
     * Zeigt nur erste 4 und letzte 4 Zeichen
     * @param id Die zu maskierende ID
     * @return Maskierte ID (z.B. "123e****4000")
     */
    private String maskId(String id) {
        if (id == null || id.length() < 8) {
            return "****";
        }
        return id.substring(0, 4) + "****" + id.substring(id.length() - 4);
    }
}