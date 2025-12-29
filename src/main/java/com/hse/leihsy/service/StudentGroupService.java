package com.hse.leihsy.service;

import com.hse.leihsy.mapper.StudentGroupMapper;
import com.hse.leihsy.model.dto.CreateStudentGroupDTO;
import com.hse.leihsy.model.dto.StudentGroupDTO;
import com.hse.leihsy.model.dto.UpdateStudentGroupDTO;
import com.hse.leihsy.model.entity.StudentGroup;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.StudentGroupRepository;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

/**
 * Service für StudentGroup Verwaltung
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentGroupService {

    private final StudentGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final StudentGroupMapper groupMapper;
    private final UserService userService;

    // ========================================
    // GET METHODEN
    // ========================================

    /**
     * Holt alle aktiven Gruppen (Admin-Funktion)
     */
    public List<StudentGroupDTO> getAllGroups() {
        List<StudentGroup> groups = groupRepository.findAllActive();
        return enrichWithBookingCounts(groupMapper.toDTOList(groups));
    }

    /**
     * Holt eine Gruppe per ID
     */
    public StudentGroupDTO getGroupById(Long id) {
        StudentGroup group = findActiveGroupById(id);
        StudentGroupDTO dto = groupMapper.toDTO(group);
        dto.setActiveBookingsCount((int) groupRepository.countActiveBookingsByGroupId(id));
        return dto;
    }

    /**
     * Holt alle Gruppen des aktuellen Users (als Mitglied)
     */
    public List<StudentGroupDTO> getMyGroups() {
        User currentUser = userService.getCurrentUser();
        List<StudentGroup> groups = groupRepository.findByMemberId(currentUser.getId());
        return enrichWithBookingCounts(groupMapper.toDTOList(groups));
    }

    /**
     * Holt alle Gruppen die ein User erstellt hat
     */
    public List<StudentGroupDTO> getGroupsCreatedByUser(Long userId) {
        List<StudentGroup> groups = groupRepository.findByCreatedById(userId);
        return enrichWithBookingCounts(groupMapper.toDTOList(groups));
    }

    /**
     * Sucht Gruppen nach Name
     */
    public List<StudentGroupDTO> searchGroups(String query) {
        List<StudentGroup> groups = groupRepository.searchByName(query);
        return enrichWithBookingCounts(groupMapper.toDTOList(groups));
    }

    // ========================================
    // CREATE METHODE
    // ========================================

    /**
     * Erstellt eine neue Gruppe
     * Der aktuelle User wird automatisch als Ersteller und erstes Mitglied gesetzt
     */
    @Transactional
    public StudentGroupDTO createGroup(CreateStudentGroupDTO createDTO) {
        User currentUser = userService.getCurrentUser();

        // Pruefe auf Duplikat-Namen
        if (groupRepository.findByNameIgnoreCase(createDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Eine Gruppe mit diesem Namen existiert bereits");
        }

        // Erstelle Gruppe
        StudentGroup group = StudentGroup.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .createdBy(currentUser)
                .members(new HashSet<>())
                .build();

        // Fuege Ersteller als erstes Mitglied hinzu
        group.addMember(currentUser);

        // Fuege weitere Mitglieder hinzu (falls angegeben)
        if (createDTO.getMemberIds() != null && !createDTO.getMemberIds().isEmpty()) {
            for (Long memberId : createDTO.getMemberIds()) {
                if (!memberId.equals(currentUser.getId())) { // Ersteller ist schon drin
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + memberId));
                    group.addMember(member);
                }
            }
        }

        StudentGroup saved = groupRepository.save(group);
        log.info("Gruppe erstellt: '{}' (ID: {}) von User {} mit {} Mitgliedern",
                saved.getName(), saved.getId(), currentUser.getName(), saved.getMemberCount());

        return groupMapper.toDTO(saved);
    }

    // ========================================
    // UPDATE METHODEN
    // ========================================

    /**
     * Aktualisiert Gruppen-Details (nur Owner)
     */
    @Transactional
    public StudentGroupDTO updateGroup(Long groupId, UpdateStudentGroupDTO updateDTO) {
        StudentGroup group = findActiveGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        // Nur Owner darf bearbeiten
        if (!group.isOwner(currentUser)) {
            throw new IllegalStateException("Nur der Ersteller kann die Gruppe bearbeiten");
        }

        // Name aktualisieren (wenn angegeben und anders)
        if (updateDTO.getName() != null && !updateDTO.getName().equals(group.getName())) {
            // Pruefe auf Duplikat
            if (groupRepository.findByNameIgnoreCase(updateDTO.getName()).isPresent()) {
                throw new IllegalArgumentException("Eine Gruppe mit diesem Namen existiert bereits");
            }
            group.setName(updateDTO.getName());
        }

        // Beschreibung aktualisieren (wenn angegeben)
        if (updateDTO.getDescription() != null) {
            group.setDescription(updateDTO.getDescription());
        }

        StudentGroup saved = groupRepository.save(group);
        log.info("Gruppe aktualisiert: '{}' (ID: {})", saved.getName(), saved.getId());

        return groupMapper.toDTO(saved);
    }

    /**
     * Fuegt ein Mitglied zur Gruppe hinzu (nur Owner)
     */
    @Transactional
    public StudentGroupDTO addMember(Long groupId, Long userId) {
        StudentGroup group = findActiveGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        // Nur Owner darf Mitglieder hinzufuegen
        if (!group.isOwner(currentUser)) {
            throw new IllegalStateException("Nur der Ersteller kann Mitglieder hinzufügen");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (group.isMember(newMember)) {
            throw new IllegalArgumentException("User ist bereits Mitglied der Gruppe");
        }

        group.addMember(newMember);
        StudentGroup saved = groupRepository.save(group);

        log.info("Mitglied hinzugefügt: User {} zu Gruppe '{}' (ID: {})",
                newMember.getName(), group.getName(), group.getId());

        return groupMapper.toDTO(saved);
    }

    /**
     * Entfernt ein Mitglied aus der Gruppe (Owner oder das Mitglied selbst)
     */
    @Transactional
    public StudentGroupDTO removeMember(Long groupId, Long userId) {
        StudentGroup group = findActiveGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        // Owner oder das Mitglied selbst darf entfernen
        boolean isOwner = group.isOwner(currentUser);
        boolean isSelf = currentUser.getId().equals(userId);

        if (!isOwner && !isSelf) {
            throw new IllegalStateException("Keine Berechtigung, dieses Mitglied zu entfernen");
        }

        User memberToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (!group.removeMember(memberToRemove)) {
            throw new IllegalArgumentException("Der Ersteller kann nicht aus der Gruppe entfernt werden");
        }

        StudentGroup saved = groupRepository.save(group);

        log.info("Mitglied entfernt: User {} aus Gruppe '{}' (ID: {})",
                memberToRemove.getName(), group.getName(), group.getId());

        return groupMapper.toDTO(saved);
    }

    // ========================================
    // DELETE METHODE
    // ========================================

    /**
     * Loescht eine Gruppe (Soft-Delete, nur Owner)
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        StudentGroup group = findActiveGroupById(groupId);
        User currentUser = userService.getCurrentUser();

        // Nur Owner darf loeschen
        if (!group.isOwner(currentUser)) {
            throw new IllegalStateException("Nur der Ersteller kann die Gruppe löschen");
        }

        // Pruefe ob noch aktive Bookings existieren
        long activeBookings = groupRepository.countActiveBookingsByGroupId(groupId);
        if (activeBookings > 0) {
            throw new IllegalStateException(
                    "Gruppe kann nicht gelöscht werden - es existieren noch " + activeBookings + " aktive Buchungen");
        }

        group.setDeletedAt(LocalDateTime.now());
        groupRepository.save(group);

        log.info("Gruppe gelöscht: '{}' (ID: {}) von User {}",
                group.getName(), group.getId(), currentUser.getName());
    }

    // ========================================
    // HELPER METHODEN
    // ========================================

    /**
     * Findet aktive Gruppe per ID oder wirft Exception
     */
    private StudentGroup findActiveGroupById(Long id) {
        return groupRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Gruppe nicht gefunden mit ID: " + id));
    }

    /**
     * Setzt die activeBookingsCount für eine Liste von DTOs
     */
    private List<StudentGroupDTO> enrichWithBookingCounts(List<StudentGroupDTO> dtos) {
        for (StudentGroupDTO dto : dtos) {
            dto.setActiveBookingsCount((int) groupRepository.countActiveBookingsByGroupId(dto.getId()));
        }
        return dtos;
    }

    /**
     * Prueft ob ein User Mitglied einer Gruppe ist (für Booking-Validierung)
     */
    public boolean isUserMemberOfGroup(Long groupId, Long userId) {
        return groupRepository.isUserMemberOfGroup(groupId, userId);
    }

    /**
     * Holt eine Gruppe Entity (für interne Nutzung, z.B. im BookingService)
     */
    public StudentGroup getGroupEntityById(Long id) {
        return findActiveGroupById(id);
    }
}