package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.BookingTransaction;
import com.hse.leihsy.model.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingTransactionRepository extends JpaRepository<BookingTransaction, Long> {

    Optional<BookingTransaction> findByToken(String token);

    boolean existsByToken(String token);

    /**
     * Macht alle alten, ungenutzten Tokens für eine bestimmte Buchung und einen Typ ungültig (Soft-Delete).
     * Wird aufgerufen, bevor ein neuer Token generiert wird.
     */
    @Modifying
    @Query("UPDATE BookingTransaction t SET t.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE t.booking.id = :bookingId " +
            "AND t.transactionType = :type " +
            "AND t.usedAt IS NULL " +
            "AND t.deletedAt IS NULL")
    void invalidatePreviousTokens(@Param("bookingId") Long bookingId, @Param("type") TransactionType type);
}