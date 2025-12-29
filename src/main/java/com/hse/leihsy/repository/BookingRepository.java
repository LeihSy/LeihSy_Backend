package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Buchungen eines Users (als Entleiher)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    // Gelöschte/Stornierte Buchungen eines Users (als Entleiher)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.deletedAt IS NOT NULL ORDER BY b.deletedAt DESC")
    List<Booking> findDeletedByUserId(@Param("userId") Long userId);

    // Buchungen für einen Verleiher
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Booking> findByLenderId(@Param("lenderId") Long lenderId);

    // Offene Anfragen für einen Verleiher (PENDING = noch keine proposed_pickups)
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId " +
            "AND b.proposedPickups IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findPendingByLenderId(@Param("lenderId") Long lenderId);

    // Offene Anfragen für einen Verleiher mit optionaler Filterung nach Item
    // (PENDING = keine proposed_pickups, optional itemId zur Eingrenzung)
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId " +
            "AND (:itemId IS NULL OR b.item.id = :itemId) " + // Optionaler Filter
            "AND b.proposedPickups IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt ASC") // Sortierung: Älteste zuerst (dringend!)
    List<Booking> findPendingByLenderIdAndOptionalItem(
            @Param("lenderId") Long lenderId,
            @Param("itemId") Long itemId
    );


    // Aktive Buchungen eines Items (für Verfügbarkeitsprüfung)
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findActiveByItemId(@Param("itemId") Long itemId);

    // Buchungen die einen Zeitraum überlappen
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findOverlappingBookings(
            @Param("itemId") Long itemId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Buchungen die bald ablaufen (Erinnerung) - für Email-Reminder
    @Query("SELECT b FROM Booking b WHERE b.endDate BETWEEN :now AND :reminderDate " +
            "AND b.returnDate IS NULL " +
            "AND b.distributionDate IS NOT NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findDueSoon(
            @Param("now") LocalDateTime now,
            @Param("reminderDate") LocalDateTime reminderDate);

    // Überfällige Buchungen
    @Query("SELECT b FROM Booking b WHERE b.endDate < :now " +
            "AND b.returnDate IS NULL " +
            "AND b.distributionDate IS NOT NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findOverdue(@Param("now") LocalDateTime now);

    // Buchungen die länger als 24h PENDING sind (für Auto-Cancel Cronjob)
    @Query("SELECT b FROM Booking b WHERE b.createdAt < :threshold " +
            "AND b.proposedPickups IS NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findPendingOlderThan(@Param("threshold") LocalDateTime threshold);

    // Buchungen die länger als 24h CONFIRMED sind ohne Abholung (für Auto-Expire Cronjob)
    @Query("SELECT b FROM Booking b WHERE b.confirmedPickup < :threshold " +
            "AND b.distributionDate IS NULL " +
            "AND b.deletedAt IS NULL")
    List<Booking> findConfirmedNotPickedUpOlderThan(@Param("threshold") LocalDateTime threshold);


    // Bevorstehende Buchungen eines Verleihers
    // (CONFIRMED = Abholung bestätigt, aber noch nicht abgeholt)
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId " +
            "AND b.confirmedPickup IS NOT NULL " +
            "AND b.distributionDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.confirmedPickup ASC") // Sortierung: Nächste Abholung zuerst
    List<Booking> findUpcomingByLenderId(@Param("lenderId") Long lenderId);

    // Aktive Buchungen eines Verleihers
    // (Bereits abgeholt, aber noch nicht zurückgegeben)
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId " +
            "AND b.distributionDate IS NOT NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.endDate ASC") // Sortierung: Nächstes Rückgabedatum zuerst
    List<Booking> findActiveByLenderId(@Param("lenderId") Long lenderId);

    // Überfällige Buchungen eines Verleihers
    // (Subset der aktiven Buchungen mit überschrittenem Enddatum)
    @Query("SELECT b FROM Booking b WHERE b.lender.id = :lenderId " +
            "AND b.distributionDate IS NOT NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.endDate < :now " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.endDate ASC")
    List<Booking> findOverdueByLenderId(
            @Param("lenderId") Long lenderId,
            @Param("now") LocalDateTime now
    );


    // Alle Bookings nach berechnetem Status
    // PENDING: Noch keine proposedPickups UND < 24h alt
    @Query("SELECT b FROM Booking b WHERE b.proposedPickups IS NULL " +
            "AND b.confirmedPickup IS NULL " +
            "AND b.distributionDate IS NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "AND b.createdAt > :threshold " +  // NEU: Nicht älter als 24h
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllPending(@Param("threshold") LocalDateTime threshold);

    // CANCELLED: > 24h alt, noch nicht bestätigt
    @Query("SELECT b FROM Booking b WHERE b.proposedPickups IS NULL " +
            "AND b.confirmedPickup IS NULL " +
            "AND b.distributionDate IS NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "AND b.createdAt <= :threshold " +  // Älter als 24h
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllCancelled(@Param("threshold") LocalDateTime threshold);

    // EXPIRED: confirmedPickup > 24h alt, aber nicht abgeholt
    @Query("SELECT b FROM Booking b WHERE b.confirmedPickup IS NOT NULL " +
            "AND b.confirmedPickup < :threshold " +  // confirmedPickup > 24h alt
            "AND b.distributionDate IS NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllExpired(@Param("threshold") LocalDateTime threshold);

    // CONFIRMED: confirmedPickup gesetzt, < 24h alt, nicht abgeholt
    @Query("SELECT b FROM Booking b WHERE b.confirmedPickup IS NOT NULL " +
            "AND b.confirmedPickup >= :threshold " +  // confirmedPickup < 24h alt
            "AND b.distributionDate IS NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllConfirmed(@Param("threshold") LocalDateTime threshold);

    // PICKED_UP: Ausgegeben, aber noch nicht zurück
    @Query("SELECT b FROM Booking b WHERE b.distributionDate IS NOT NULL " +
            "AND b.returnDate IS NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllPickedUp();

    // RETURNED: Zurückgegeben
    @Query("SELECT b FROM Booking b WHERE b.returnDate IS NOT NULL " +
            "AND b.deletedAt IS NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllReturned();

    // REJECTED: Soft-deleted
    @Query("SELECT b FROM Booking b WHERE b.deletedAt IS NOT NULL " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findAllRejected();

    // Alle aktiven Bookings (ohne gelöschte)
    @Query("SELECT b FROM Booking b WHERE b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Booking> findAllActive();

    @Query("SELECT b FROM Booking b WHERE b.studentGroup.id = :groupId AND b.deletedAt IS NULL")
    List<Booking> findByStudentGroupId(@Param("groupId") Long groupId);
}