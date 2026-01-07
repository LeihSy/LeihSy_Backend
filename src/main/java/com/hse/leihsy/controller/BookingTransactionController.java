package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.TransactionDTO;
import com.hse.leihsy.service.BookingTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "QR-Code Token System für sichere Ausgabe/Rückgabe")
public class BookingTransactionController {

    private final BookingTransactionService transactionService;

    @Operation(summary = "Token generieren (Student)",
            description = "Erstellt einen temporären Token (15min). Der Typ (PICKUP oder RETURN) wird automatisch anhand des Buchungsstatus ermittelt.")
    @PostMapping("/bookings/{bookingId}/transactions")
    public ResponseEntity<TransactionDTO> generateToken(@PathVariable Long bookingId) {
        TransactionDTO transaction = transactionService.generateToken(bookingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @Operation(summary = "Token einlösen (Verleiher)",
            description = "Löst den Token ein (setzt Status auf USED) und aktualisiert die Buchung (CONFIRMED -> PICKED_UP oder PICKED_UP -> RETURNED).")
    @PatchMapping("/transactions/{token}")
    public ResponseEntity<BookingDTO> redeemToken(@PathVariable String token) {
        BookingDTO updatedBooking = transactionService.executeTransaction(token);
        return ResponseEntity.ok(updatedBooking);
    }
}