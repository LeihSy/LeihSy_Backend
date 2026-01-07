package com.hse.leihsy.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_transactions", indexes = {
        @Index(name = "idx_token_expiry", columnList = "token, expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTransaction extends BaseEntity {

    @Column(name = "token", length = 8, unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Prüft, ob der Token noch gültig ist (nicht abgelaufen und nicht benutzt).
     */
    public boolean isValid() {
        return usedAt == null && LocalDateTime.now().isBefore(expiresAt) && getDeletedAt() == null;
    }
}