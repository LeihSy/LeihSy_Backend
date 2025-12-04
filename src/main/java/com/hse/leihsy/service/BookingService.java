package com.hse.leihsy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
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
    private final BookingMapper bookingMapper;

    // ========================================
    // GET METHODEN - MIT DTOs
    // ========================================

    /**
     * Holt eine Booking als DTO anhand der ID
     */
    public BookingDTO getBookingDTOById(Long id) {
        Booking booking = getBookingById(id);
        return bookingMapper.toDTO(booking);
    }

    /**
     * Holt alle Bookings eines Users als DTOs (als Student/Entleiher)
     */
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle Bookings eines Verleihers als DTOs
     */
    public List<BookingDTO> getBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle PENDING Bookings eines Verleihers als DTOs
     */
    public List<BookingDTO> getPendingBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findPendingByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle überfälligen Bookings als DTOs
     */
    public List<BookingDTO> getOverdueBookings() {
        List<Booking> bookings = bookingRepository.findOverdue(LocalDateTime.now());
        return bookingMapper.toDTOList(bookings);
    }

    // ========================================
    // GET METHODEN - ENTITIES (für interne Nutzung)
    // ========================================

    /**
     * Holt eine Booking Entity anhand der ID (nur intern nutzen!)
     */
    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    // ========================================
    // CREATE METHODE
    // ========================================

    /**
     * Erstellt eine neue Booking und gibt DTO zurück
     */
    @Transactional
    public BookingDTO createBooking(Long userId, Long itemId, LocalDateTime startDate,
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

        // Booking erstellen
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setLender(item.getLender());
        booking.setItem(item);
        booking.setMessage(message);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setMessage(message);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);  // ← DTO zurückgeben!
    }

    /**
     * Prüft ob ein Item im gewünschten Zeitraum verfügbar ist
     */
    private boolean checkAvailability(Long itemId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> conflictingBookings = bookingRepository.findAll().stream()
                .filter(booking -> booking.getItem().getId().equals(itemId))
                .filter(booking -> booking.getDeletedAt() == null)
                .filter(booking -> {
                    BookingStatus status = booking.calculateStatus();
                    return status == BookingStatus.PENDING
                            || status == BookingStatus.CONFIRMED
                            || status == BookingStatus.PICKED_UP;
                })
                .filter(booking -> {
                    return !(endDate.isBefore(booking.getStartDate())
                            || startDate.isAfter(booking.getEndDate()));
                })
                .toList();

        return conflictingBookings.isEmpty();
    }

    // ========================================
    // UPDATE METHODEN - MIT DTOs
    // ========================================

    /**
     * Verleiher bestätigt Booking und schlägt Abholtermine vor
     */
    @Transactional
    public BookingDTO confirmBooking(Long id, List<LocalDateTime> proposedPickups) {
        Booking booking = getBookingById(id);

        if (booking.getConfirmedPickup() != null) {
            throw new IllegalStateException("Booking already confirmed");
        }
        if (booking.getDeletedAt() != null) {
            throw new IllegalStateException("Cannot confirm cancelled booking");
        }

        String pickupsJson = convertPickupsToJson(proposedPickups);
        booking.setProposedPickups(pickupsJson);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Student wählt einen der vorgeschlagenen Abholtermine aus
     */
    @Transactional
    public BookingDTO selectPickupTime(Long id, LocalDateTime selectedPickup) {
        Booking booking = getBookingById(id);

        List<LocalDateTime> proposedPickups = convertJsonToPickups(booking.getProposedPickups());
        if (!proposedPickups.contains(selectedPickup)) {
            throw new RuntimeException("Selected pickup time is not in proposed pickups");
        }

        booking.setConfirmedPickup(selectedPickup);
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Gegenvorschlag machen (Ping-Pong)
     */
    @Transactional
    public BookingDTO proposeNewPickups(Long id, Long proposerId, List<LocalDateTime> proposedPickups) {
        Booking booking = getBookingById(id);

        BookingStatus status = booking.calculateStatus();
        if (status == BookingStatus.RETURNED || status == BookingStatus.REJECTED
                || status == BookingStatus.CANCELLED || status == BookingStatus.EXPIRED) {
            throw new RuntimeException("Booking is already closed");
        }

        String pickupsJson = convertPickupsToJson(proposedPickups);
        booking.setProposedPickups(pickupsJson);
        booking.setProposalBy(userRepository.findById(proposerId).orElseThrow());
        booking.setConfirmedPickup(null);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Verleiher lehnt Booking ab
     */
    @Transactional
    public BookingDTO rejectBooking(Long id) {
        Booking booking = getBookingById(id);

        if (booking.calculateStatus() == BookingStatus.RETURNED) {
            throw new RuntimeException("Cannot reject a returned booking");
        }

        if (booking.calculateStatus() != BookingStatus.PENDING &&
                booking.calculateStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Can only reject PENDING or CONFIRMED bookings");
        }
        if (booking.getDeletedAt() != null) {
            throw new IllegalStateException("Booking already cancelled");
        }

        booking.setDeletedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Verleiher dokumentiert Ausgabe
     */
    @Transactional
    public BookingDTO recordPickup(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getConfirmedPickup() == null) {
            throw new RuntimeException("No pickup time confirmed yet");
        }

        if (booking.getDistributionDate() != null) {
            throw new RuntimeException("Item already picked up");
        }

        booking.setDistributionDate(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Verleiher dokumentiert Rückgabe
     */
    @Transactional
    public BookingDTO recordReturn(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getDistributionDate() == null) {
            throw new RuntimeException("Item not picked up yet");
        }

        if (booking.getReturnDate() != null) {
            throw new RuntimeException("Item already returned");
        }

        booking.setReturnDate(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Student oder Admin storniert Booking
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

    private String convertPickupsToJson(List<LocalDateTime> pickups) {
        try {
            return objectMapper.writeValueAsString(pickups);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert pickups to JSON", e);
        }
    }

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