package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für StudentGroup Entity
 */
@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {

    /**
     * Findet alle aktiven Gruppen (nicht geloescht)
     */
    @Query("SELECT g FROM StudentGroup g WHERE g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    List<StudentGroup> findAllActive();

    /**
     * Findet eine aktive Gruppe per ID
     */
    @Query("SELECT g FROM StudentGroup g WHERE g.id = :id AND g.deletedAt IS NULL")
    Optional<StudentGroup> findActiveById(@Param("id") Long id);

    /**
     * Findet alle Gruppen die von einem User erstellt wurden
     */
    @Query("SELECT g FROM StudentGroup g WHERE g.createdBy.id = :userId AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    List<StudentGroup> findByCreatedById(@Param("userId") Long userId);

    /**
     * Findet alle Gruppen in denen ein User Mitglied ist
     */
    @Query("SELECT g FROM StudentGroup g JOIN g.members m WHERE m.id = :userId AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    List<StudentGroup> findByMemberId(@Param("userId") Long userId);

    /**
     * Findet Gruppe per Name (für Duplikat-Check)
     */
    @Query("SELECT g FROM StudentGroup g WHERE LOWER(g.name) = LOWER(:name) AND g.deletedAt IS NULL")
    Optional<StudentGroup> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Prueft ob ein User Mitglied einer bestimmten Gruppe ist
     */
    @Query("SELECT COUNT(g) > 0 FROM StudentGroup g JOIN g.members m WHERE g.id = :groupId AND m.id = :userId AND g.deletedAt IS NULL")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * Zaehlt die aktiven Bookings einer Gruppe
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.studentGroup.id = :groupId AND b.deletedAt IS NULL AND b.returnDate IS NULL")
    long countActiveBookingsByGroupId(@Param("groupId") Long groupId);

    /**
     * Sucht Gruppen nach Name (Teilstring, case-insensitive)
     */
    @Query("SELECT g FROM StudentGroup g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) AND g.deletedAt IS NULL ORDER BY g.name")
    List<StudentGroup> searchByName(@Param("search") String search);
}