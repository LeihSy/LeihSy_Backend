package com.hse.leihsy.service.security;

import com.hse.leihsy.model.entity.StudentGroup;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.StudentGroupRepository;
import com.hse.leihsy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Security Service für StudentGroup-Autorisierung
 *
 * Prüft ob ein User berechtigt ist, eine Gruppe zu sehen oder zu ändern.
 */
@Service("studentGroupSecurityService")
@RequiredArgsConstructor
@Slf4j
public class StudentGroupSecurityService {

    private final StudentGroupRepository groupRepository;
    private final UserService userService;

    /**
     * Prüft ob der aktuelle User eine Gruppe sehen darf.
     *
     * Erlaubt wenn:
     * - User ist Mitglied der Gruppe
     * - User ist Owner der Gruppe
     *
     * @param groupId ID der Gruppe
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canView(Long groupId, Authentication authentication) {
        if (authentication == null || groupId == null) {
            return false;
        }

        StudentGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();

        // User ist Mitglied oder Owner
        return group.isMember(currentUser) || group.isOwner(currentUser);
    }

    /**
     * Prüft ob der aktuelle User eine Gruppe ändern darf.
     *
     * Erlaubt wenn:
     * - User ist Owner (createdBy) der Gruppe
     *
     * @param groupId ID der Gruppe
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canUpdate(Long groupId, Authentication authentication) {
        if (authentication == null || groupId == null) {
            return false;
        }

        StudentGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();
        return group.isOwner(currentUser);
    }

    /**
     * Prüft ob der aktuelle User eine Gruppe löschen darf.
     *
     * Erlaubt wenn:
     * - User ist Owner (createdBy) der Gruppe
     *
     * @param groupId ID der Gruppe
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canDelete(Long groupId, Authentication authentication) {
        // Gleiche Logik wie canUpdate
        return canUpdate(groupId, authentication);
    }

    /**
     * Prüft ob der aktuelle User Mitglieder hinzufügen/entfernen darf.
     *
     * Erlaubt wenn:
     * - User ist Owner (createdBy) der Gruppe
     * - ODER User entfernt sich selbst
     *
     * @param groupId ID der Gruppe
     * @param targetUserId ID des zu entfernenden Users
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canManageMembers(Long groupId, Long targetUserId, Authentication authentication) {
        if (authentication == null || groupId == null) {
            return false;
        }

        StudentGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();

        // Owner kann immer Mitglieder verwalten
        if (group.isOwner(currentUser)) {
            return true;
        }

        // User kann sich selbst entfernen (wenn targetUserId == currentUser.id)
        if (targetUserId != null && targetUserId.equals(currentUser.getId())) {
            return true;
        }

        return false;
    }
}
