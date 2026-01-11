package com.hse.leihsy.service;

import com.hse.leihsy.exception.ConflictException;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.exception.TokenExpiredException;
import com.hse.leihsy.exception.UnauthorizedException;
import com.hse.leihsy.exception.ValidationException;
import com.hse.leihsy.exception.InvalidBookingStatusException; // Wichtig: Import prüfen
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

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TOKEN_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    /**
     * Student generiert Token.
     * Typ (PICKUP/RETURN) wird automatisch anhand des Status bestimmt.
     */
    @Transactional
    public TransactionDTO generateToken(Long bookingId) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        // 1. Autorisierung
        boolean isAuthorized = booking.getUser().getId().equals(currentUser.getId()) ||
                (booking.getStudentGroup() != null && booking.getStudentGroup().isMember(currentUser));

        if (!isAuthorized) {
            if (!booking.getLender().getId().equals(currentUser.getId())) {
                throw new UnauthorizedException("Nur der Entleiher darf den QR-Code für diese Buchung generieren.");
            }
        }

        // 2. Typ bestimmen
        TransactionType type;
        BookingStatus status = booking.calculateStatus(); // Nutzt die Logik aus Booking.java

        if (status == BookingStatus.CONFIRMED) {
            type = TransactionType.PICKUP;
        } else if (status == BookingStatus.PICKED_UP) {
            type = TransactionType.RETURN;
        } else {
            throw new InvalidBookingStatusException("QR-Code kann im Status " + status + " nicht generiert werden.");
        }

        // Wenn wir schon einen gültigen Token haben, geben wir diesen einfach zurück,
        // anstatt einen neuen zu generieren.
        var existingToken = transactionRepository.findValidToken(bookingId, type);
        if (existingToken.isPresent()) {
            log.info("Returning existing valid token for Booking {}", bookingId);
            return mapToDTO(existingToken.get());
        }

        // 3. Alte Tokens invalidieren (Cleanup) - Passiert nur, wenn kein gültiger Token da war
        transactionRepository.invalidatePreviousTokens(bookingId, type);

        // 4. Token generieren
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
        log.info("Generated NEW token for Booking {} (type: {})", bookingId, type);

        return mapToDTO(saved);
    }

    /**
     * Lender scannt Token.
     */
    @Transactional
    public BookingDTO executeTransaction(String token) {
        User currentUser = userService.getCurrentUser();

        // 1. Token suchen
        BookingTransaction transaction = transactionRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("QR-Code nicht gefunden oder ungültig."));

        // 2. Token Validierung
        if (!transaction.isValid()) {
            if (transaction.getUsedAt() != null) {
                throw new ConflictException("Dieser QR-Code wurde bereits verwendet.");
            }
            if (LocalDateTime.now().isAfter(transaction.getExpiresAt())) {
                throw new TokenExpiredException("Dieser QR-Code ist abgelaufen. Bitte generieren Sie einen neuen.");
            }
            throw new ValidationException("QR-Code ungültig.");
        }

        Booking booking = transaction.getBooking();

        // 3. Autorisierung: Nur der Lender darf scannen
        if (!booking.getLender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Nur der zuständige Verleiher darf diesen Code scannen.");
        }

        // 4. Status der Buchung erneut prüfen
        BookingStatus status = booking.calculateStatus();
        if (transaction.getTransactionType() == TransactionType.PICKUP && status != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException("Abholung nicht möglich. Status ist: " + status);
        }
        if (transaction.getTransactionType() == TransactionType.RETURN && status != BookingStatus.PICKED_UP) {
            throw new InvalidBookingStatusException("Rückgabe nicht möglich. Status ist: " + status);
        }

        // 5. Aktion ausführen
        BookingDTO updatedBooking;
        if (transaction.getTransactionType() == TransactionType.PICKUP) {
            updatedBooking = bookingService.recordPickup(booking.getId());
        } else {
            updatedBooking = bookingService.recordReturn(booking.getId());
        }

        // 6. Token als benutzt markieren
        transaction.setUsedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        log.info("Transaction executed: Token {} scanned by Lender {} for Booking {}",
                token, currentUser.getUniqueId(), booking.getId());

        return updatedBooking;
    }

    // --- Helper Methods ---

    private String generateUniqueToken() {
        String token;
        int retries = 0;
        do {
            if (retries > 3) throw new RuntimeException("Systemfehler bei Token-Generierung");
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