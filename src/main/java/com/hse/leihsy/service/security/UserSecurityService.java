package com.hse.leihsy.service.security;

import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Security Service für User-Autorisierung
 *
 * Prüft ob ein User berechtigt ist, eigene oder fremde User-Daten zu sehen.
 */
@Service("userSecurityService")
@RequiredArgsConstructor
@Slf4j
public class UserSecurityService {

    private final UserService userService;

    /**
     * Prüft ob der aktuelle User einen anderen User sehen darf.
     *
     * Erlaubt wenn:
     * - User ist der User selbst
     *
     * @param userId ID des Users
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canView(Long userId, Authentication authentication) {
        if (authentication == null || userId == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();
        return currentUser.getId().equals(userId);
    }
}
