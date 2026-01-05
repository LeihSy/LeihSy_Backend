package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.InsyImportItem;
import com.hse.leihsy.model.entity.InsyImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsyImportItemRepository extends JpaRepository<InsyImportItem, Long> {

    /**
     * Findet Import-Eintrag anhand der InSy-ID
     */
    Optional<InsyImportItem> findByInsyId(Long insyId);

    /**
     * Prueft ob ein Eintrag mit dieser InSy-ID existiert
     */
    boolean existsByInsyId(Long insyId);

    /**
     * Alle Eintraege mit bestimmtem Status
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.status = :status AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<InsyImportItem> findByStatus(@Param("status") InsyImportStatus status);

    /**
     * Alle PENDING Eintraege (fuer Admin-Dashboard)
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.status = 'PENDING' AND i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<InsyImportItem> findAllPending();

    /**
     * Alle aktiven Eintraege (nicht geloescht)
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.deletedAt IS NULL ORDER BY i.createdAt DESC")
    List<InsyImportItem> findAllActive();

    /**
     * Alle bereits importierten Eintraege
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.status IN ('IMPORTED', 'UPDATED') AND i.deletedAt IS NULL ORDER BY i.updatedAt DESC")
    List<InsyImportItem> findAllImported();

    /**
     * Alle abgelehnten Eintraege
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.status = 'REJECTED' AND i.deletedAt IS NULL ORDER BY i.updatedAt DESC")
    List<InsyImportItem> findAllRejected();

    /**
     * Zaehlt PENDING Eintraege (fuer Badge im Frontend)
     */
    @Query("SELECT COUNT(i) FROM InsyImportItem i WHERE i.status = 'PENDING' AND i.deletedAt IS NULL")
    long countPending();

    /**
     * Findet alle PENDING Eintraege mit bestimmten IDs (fuer Batch-Import)
     */
    @Query("SELECT i FROM InsyImportItem i WHERE i.id IN :ids AND i.status = 'PENDING' AND i.deletedAt IS NULL")
    List<InsyImportItem> findPendingByIds(@Param("ids") List<Long> ids);
}