package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Findet User anhand der Keycloak unique_id (aus JWT Token)
     * @param uniqueId Die Keycloak Subject ID
     * @return Optional<User>
     */
    Optional<User> findByUniqueId(String uniqueId);

    /**
     * Prüft ob ein User mit dieser unique_id existiert
     * @param uniqueId Die Keycloak Subject ID
     * @return true wenn User existiert
     */
    boolean existsByUniqueId(String uniqueId);
}