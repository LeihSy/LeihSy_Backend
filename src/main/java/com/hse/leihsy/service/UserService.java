package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        return userRepository.save(user);
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
                .orElseThrow(() -> new RuntimeException("User not found with name: " + name));
    }

    /**
     * Holt den aktuell eingeloggten User aus dem JWT Token
     * @return Current User
     */
//    public User getCurrentUser() {
//        // 1. Authentication aus SecurityContext holen
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            throw new RuntimeException("Access Denied: No authentication found");
//        }
//
//        // 2. JWT Token extrahieren
//        if (!(authentication.getPrincipal() instanceof Jwt)) {
//            throw new RuntimeException("Access Denied: Invalid authentication type");
//        }
//
//        Jwt jwt = (Jwt) authentication.getPrincipal();
//
//        // 3. Keycloak UUID aus Token holen
//        String keycloakId = jwt.getSubject();
//
//        // 4. User aus DB holen (oder automatisch anlegen)
//        User user = userRepository.findByUniqueId(keycloakId)
//                .orElseGet(() -> {
//                    // User existiert noch nicht → automatisch anlegen!
//                    String name = jwt.getClaim("preferred_username");
//                    String email = jwt.getClaim("email");
//
//                    return createUser(keycloakId, name, email);
//                });
//        return user;
//    }

    public User getCurrentUser() {
        System.out.println("--- UserService.getCurrentUser() START ---");

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("🔍 Authentication: " + authentication);

            if (authentication == null) {
                System.err.println("❌ Authentication is NULL!");
                throw new RuntimeException("Access Denied: No authentication found");
            }

            System.out.println("✅ Authentication found: " + authentication.getClass().getName());
            System.out.println("✅ Principal type: " + authentication.getPrincipal().getClass().getName());

            if (!authentication.isAuthenticated()) {
                System.err.println("❌ User is not authenticated!");
                throw new RuntimeException("Access Denied: User not authenticated");
            }

            if (!(authentication.getPrincipal() instanceof Jwt)) {
                System.err.println("❌ Principal is not a JWT! Type: " + authentication.getPrincipal().getClass().getName());
                throw new RuntimeException("Access Denied: Invalid authentication type");
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakId = jwt.getSubject();

            System.out.println("✅ Keycloak ID from token: " + keycloakId);

            User user = userRepository.findByUniqueId(keycloakId)
                    .orElseGet(() -> {
                        System.out.println("⚠️ User not found in DB, creating new user...");
                        String name = jwt.getClaim("preferred_username");
                        String email = jwt.getClaim("email");
                        return createUser(keycloakId, name, email);
                    });

            System.out.println("✅ Found/Created User: ID=" + user.getId() + ", Name=" + user.getName());
            System.out.println("--- UserService.getCurrentUser() END ---");

            return user;

        } catch (Exception e) {
            System.err.println("❌ Exception in getCurrentUser(): " + e.getMessage());
            throw e;
        }
    }
}