package com.hse.leihsy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.model.entity.BookingStatus;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ========================================
    // GET METHODEN
    // ========================================

    /**
     * Holt eine Booking anhand der ID
     */
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    /**
     * Holt alle Bookings eines Users (als Student/Entleiher)
     */
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    /**
     * Holt alle Bookings eines Verleihers
     */
    public List<Booking> getBookingsByLenderId(Long lenderId) {
        return bookingRepository.findByLenderId(lenderId);
    }

    /**
     * Holt alle PENDING Bookings eines Verleihers
     */
    public List<Booking> getPendingBookingsByLenderId(Long lenderId) {
        return bookingRepository.findPendingByLenderId(lenderId);
    }

    /**
     * Holt alle überfälligen Bookings (return_date < jetzt und noch nicht zurückgegeben)
     */
    public List<Booking> getOverdueBookings() {
        return bookingRepository.findOverdue(LocalDateTime.now());
    }

    // ========================================
    // CREATE METHODE
    // ========================================

    /**
     * Erstellt eine neue Booking
     * @param userId ID des Entleihers
     * @param itemId ID des Items
     * @param startDate Gewünschter Ausleihbeginn
     * @param endDate Gewünschtes Ausleihende
     * @param message Optionale Nachricht
     * @return Die erstellte Booking
     */
    @Transactional
    public Booking createBooking(Long userId, Long itemId, LocalDateTime startDate,
                                 LocalDateTime endDate, String message) {

        // User validieren
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Item validieren
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

        // Prüfen ob Item einen Lender hat
        if (item.getLender() == null) {
            throw new RuntimeException("Item has no assigned lender");
        }

        // Verfügbarkeit prüfen
        boolean isAvailable = checkAvailability(itemId, startDate, endDate);
        if (!isAvailable) {
            throw new RuntimeException("Item is not available in the requested time range");
        }

        // Booking erstellen (Konstruktor setzt lender automatisch aus item.getLender())
        Booking booking = new Booking(user, item, startDate, endDate);
        booking.setMessage(message);

        return bookingRepository.save(booking);
    }

    /**
     * Prüft ob ein Item im gewünschten Zeitraum verfügbar ist
     */
    private boolean checkAvailability(Long itemId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> conflictingBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getItem().getId().equals(itemId))
                .filter(booking -> booking.getDeletedAt() == null) // Nur aktive Bookings
                .filter(booking -> {
                    BookingStatus status = booking.calculateStatus();
                    return status == BookingStatus.PENDING
                            || status == BookingStatus.CONFIRMED
                            || status == BookingStatus.PICKED_UP;
                })
                .filter(booking -> {
                    // Zeitraum-Überschneidung prüfen
                    return !(endDate.isBefore(booking.getStartDate())
                            || startDate.isAfter(booking.getEndDate()));
                })
                .toList();

        return conflictingBookings.isEmpty();
    }

    // ========================================
    // UPDATE METHODEN
    // ========================================

    /**
     * Verleiher bestätigt Booking und schlägt Abholtermine vor
     * @param id Booking ID
     * @param proposedPickups Liste mit vorgeschlagenen Abholterminen
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking confirmBooking(Long id, List<LocalDateTime> proposedPickups) {
        Booking booking = getBookingById(id);

        if (booking.calculateStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not in PENDING status");
        }

        String pickupsJson = convertPickupsToJson(proposedPickups);
        booking.setProposedPickups(pickupsJson);

        return bookingRepository.save(booking);
    }

    /**
     * Student wählt einen der vorgeschlagenen Abholtermine aus
     * @param id Booking ID
     * @param selectedPickup Gewählter Termin
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking selectPickupTime(Long id, LocalDateTime selectedPickup) {
        Booking booking = getBookingById(id);

        if (booking.calculateStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking is not in CONFIRMED status");
        }

        // Prüfen ob gewählter Termin in vorgeschlagenen Terminen enthalten ist
        List<LocalDateTime> proposedPickups = convertJsonToPickups(booking.getProposedPickups());
        if (!proposedPickups.contains(selectedPickup)) {
            throw new RuntimeException("Selected pickup time is not in proposed pickups");
        }

        booking.setConfirmedPickup(selectedPickup);
        return bookingRepository.save(booking);
    }

    /**
     * Gegenvorschlag machen (Ping-Pong)
     * @param id Booking ID
     * @param proposerId ID der Person die vorschlägt
     * @param proposedPickups Neue Terminvorschläge
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking proposeNewPickups(Long id, Long proposerId, List<LocalDateTime> proposedPickups) {
        Booking booking = getBookingById(id);

        BookingStatus status = booking.calculateStatus();
        if (status == BookingStatus.RETURNED || status == BookingStatus.REJECTED
                || status == BookingStatus.CANCELLED || status == BookingStatus.EXPIRED) {
            throw new RuntimeException("Booking is already closed");
        }

        String pickupsJson = convertPickupsToJson(proposedPickups);
        booking.setProposedPickups(pickupsJson);
        booking.setProposalBy(userRepository.findById(proposerId).orElseThrow());

        // confirmed_pickup zurücksetzen wenn neuer Vorschlag kommt
        booking.setConfirmedPickup(null);

        return bookingRepository.save(booking);
    }

    /**
     * Verleiher lehnt Booking ab
     * @param id Booking ID
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking rejectBooking(Long id) {
        Booking booking = getBookingById(id);

        if (booking.calculateStatus() == BookingStatus.RETURNED) {
            throw new RuntimeException("Cannot reject a returned booking");
        }

        booking.setDeletedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    /**
     * Verleiher dokumentiert Ausgabe
     * @param id Booking ID
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking recordPickup(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getConfirmedPickup() == null) {
            throw new RuntimeException("No pickup time confirmed yet");
        }

        if (booking.getDistributionDate() != null) {
            throw new RuntimeException("Item already picked up");
        }

        booking.setDistributionDate(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    /**
     * Verleiher dokumentiert Rückgabe
     * @param id Booking ID
     * @return Aktualisierte Booking
     */
    @Transactional
    public Booking recordReturn(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getDistributionDate() == null) {
            throw new RuntimeException("Item not picked up yet");
        }

        if (booking.getReturnDate() != null) {
            throw new RuntimeException("Item already returned");
        }

        booking.setReturnDate(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    /**
     * Student oder Admin storniert Booking
     * @param id Booking ID
     */
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);

        BookingStatus status = booking.calculateStatus();
        if (status == BookingStatus.PICKED_UP || status == BookingStatus.RETURNED) {
            throw new RuntimeException("Cannot cancel a booking that is already picked up or returned");
        }

        booking.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    // ========================================
    // HELPER METHODEN (JSON KONVERTIERUNG)
    // ========================================

    /**
     * Konvertiert eine Liste von LocalDateTime zu JSON String
     */
    private String convertPickupsToJson(List<LocalDateTime> pickups) {
        try {
            return objectMapper.writeValueAsString(pickups);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert pickups to JSON", e);
        }
    }

    /**
     * Konvertiert JSON String zurück zu Liste von LocalDateTime
     */
    private List<LocalDateTime> convertJsonToPickups(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<LocalDateTime>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse pickups from JSON", e);
        }
    }
}