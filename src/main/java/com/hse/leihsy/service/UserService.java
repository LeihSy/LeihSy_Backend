package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}