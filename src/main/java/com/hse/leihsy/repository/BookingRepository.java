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
}