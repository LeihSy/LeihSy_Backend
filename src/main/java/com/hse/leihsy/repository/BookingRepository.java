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

    // Buchungen für einen Verleiher
    @Query("SELECT b FROM Booking b WHERE b.receiver.id = :receiverId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<Booking> findByReceiverId(@Param("receiverId") Long receiverId);

    // Buchungen nach Status
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.deletedAt IS NULL")
    List<Booking> findByStatus(@Param("status") String status);

    // Offene Anfragen fuer einen Verleiher (PENDING)
    @Query("SELECT b FROM Booking b WHERE b.receiver.id = :receiverId AND b.status = 'PENDING' AND b.deletedAt IS NULL")
    List<Booking> findPendingByReceiverId(@Param("receiverId") Long receiverId);

    // Aktive Buchungen eines Items (für Verfuegbarkeitspruefung)
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.returnDate IS NULL AND b.deletedAt IS NULL")
    List<Booking> findActiveByItemId(@Param("itemId") Long itemId);

    // Buchungen die einen Zeitraum überlappen
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
            "AND b.returnDate IS NULL AND b.deletedAt IS NULL")
    List<Booking> findOverlappingBookings(
            @Param("itemId") Long itemId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Buchungen die bald ablaufen (Erinnerung)
    @Query("SELECT b FROM Booking b WHERE b.endDate BETWEEN :now AND :reminderDate " +
            "AND b.returnDate IS NULL AND b.status = 'PICKED_UP' AND b.deletedAt IS NULL")
    List<Booking> findDueSoon(@Param("now") LocalDateTime now, @Param("reminderDate") LocalDateTime reminderDate);

    // Überfällige Buchungen
    @Query("SELECT b FROM Booking b WHERE b.endDate < :now AND b.returnDate IS NULL " +
            "AND b.status = 'PICKED_UP' AND b.deletedAt IS NULL")
    List<Booking> findOverdue(@Param("now") LocalDateTime now);
}