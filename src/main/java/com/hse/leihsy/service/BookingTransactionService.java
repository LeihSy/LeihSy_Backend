package com.hse.leihsy.service;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.TransactionDTO;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.BookingTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingTransactionService {

    private final BookingTransactionRepository transactionRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final UserService userService;

    @Value("${app.transaction.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Ohne I, O, 1, 0 für Lesbarkeit
    private static final int TOKEN_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    /**
     * Generiert einen neuen Token für eine Buchung (nur für Verleiher).
     */
    @Transactional
    public TransactionDTO generateToken(Long bookingId, TransactionType type) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 1. Autorisierung: Nur der Verleiher darf Tokens erstellen
        if (!booking.getLender().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the lender can generate transactions for this booking");
        }

        // 2. Status-Prüfung der Buchung
        validateBookingStatusForType(booking, type);

        // 3. Alte Tokens invalidieren (Cleanup)
        transactionRepository.invalidatePreviousTokens(bookingId, type);

        // 4. Token generieren (mit Retry bei Kollision)
        String token = generateUniqueToken();

        // 5. Speichern
        BookingTransaction transaction = BookingTransaction.builder()
                .booking(booking)
                .token(token)
                .transactionType(type)
                .createdBy(currentUser)
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .build();

        BookingTransaction saved = transactionRepository.save(transaction);
        log.info("Token generated for Booking {} (Type: {}): {}", bookingId, type, token);

        return mapToDTO(saved);
    }

    /**
     * Führt die Transaktion basierend auf dem Token aus (für Student/Abholer).
     */
    @Transactional
    public BookingDTO executeTransaction(String token) {
        User currentUser = userService.getCurrentUser();

        // 1. Token suchen
        BookingTransaction transaction = transactionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        // 2. Token Validierung
        if (!transaction.isValid()) {
            if (transaction.getUsedAt() != null) {
                throw new RuntimeException("Token already used");
            }
            if (LocalDateTime.now().isAfter(transaction.getExpiresAt())) {
                throw new RuntimeException("Token expired");
            }
            throw new RuntimeException("Token invalid"); // Fallback für deletedAt
        }

        Booking booking = transaction.getBooking();

        // 3. Autorisierung: User muss der Entleiher oder Gruppenmitglied sein
        boolean isAuthorized = booking.getUser().getId().equals(currentUser.getId()) ||
                (booking.getStudentGroup() != null && booking.getStudentGroup().isMember(currentUser));

        if (!isAuthorized) {
            throw new RuntimeException("User not authorized to execute this transaction");
        }

        // 4. Status der Buchung erneut prüfen (könnte sich geändert haben)
        validateBookingStatusForType(booking, transaction.getTransactionType());

        // 5. Aktion ausführen (Delegation an BookingService)
        BookingDTO updatedBooking;
        if (transaction.getTransactionType() == TransactionType.PICKUP) {
            updatedBooking = bookingService.recordPickup(booking.getId());
        } else {
            updatedBooking = bookingService.recordReturn(booking.getId());
        }

        // 6. Token als benutzt markieren
        transaction.setUsedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        log.info("Transaction executed: Token {} used by User {} for Booking {}",
                token, currentUser.getUniqueId(), booking.getId());

        return updatedBooking;
    }

    // --- Helper Methods ---

    private void validateBookingStatusForType(Booking booking, TransactionType type) {
        BookingStatus status = booking.calculateStatus();

        if (type == TransactionType.PICKUP) {
            // Muss CONFIRMED sein
            if (status != BookingStatus.CONFIRMED) {
                throw new RuntimeException("Cannot generate PICKUP token. Booking status is " + status);
            }
        } else if (type == TransactionType.RETURN) {
            // Muss PICKED_UP sein
            if (status != BookingStatus.PICKED_UP) {
                throw new RuntimeException("Cannot generate RETURN token. Booking status is " + status);
            }
        }
    }

    private String generateUniqueToken() {
        String token;
        int retries = 0;
        do {
            if (retries > 3) throw new RuntimeException("Failed to generate unique token");
            StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
            for (int i = 0; i < TOKEN_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            token = sb.toString();
            retries++;
        } while (transactionRepository.existsByToken(token));
        return token;
    }

    private TransactionDTO mapToDTO(BookingTransaction entity) {
        return TransactionDTO.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking().getId())
                .token(entity.getToken())
                .transactionType(entity.getTransactionType())
                .expiresAt(entity.getExpiresAt())
                .createdByUserId(entity.getCreatedBy().getId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}