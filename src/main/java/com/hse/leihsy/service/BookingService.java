package com.hse.leihsy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingStatusUpdateDTO;
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
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final EmailService emailService;


    @Value("${app.base-url}")
    private String baseUrl;

    // ========================================
    // GET METHODEN - MIT DTOs
    // ========================================

    /**
     * Holt alle Bookings als DTOs (Admin-Funktion)
     */
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookingMapper.toDTOList(bookings);
    }

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
     * Holt alle Bookings eines Users als DTOs (als Student/Entleiher)
     */
    public List<BookingDTO> getBookingsByUserId(Long userId, boolean includeDeleted) {
        List<Booking> bookings;
        if (includeDeleted) {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getUser() != null && b.getUser().getId().equals(userId))
                    .toList();
        } else {
            bookings = bookingRepository.findByUserId(userId);
        }
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle gelöschten/stornierten Bookings eines Users als DTOs (als Student/Entleiher)
     */
    public List<BookingDTO> getDeletedBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findDeletedByUserId(userId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle Bookings eines Verleihers als DTOs
     */
    public List<BookingDTO> getBookingsByLenderId(Long lenderId, boolean includeDeleted) {
        List<Booking> bookings;
        if (includeDeleted) {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getLender() != null && b.getLender().getId().equals(lenderId))
                    .toList();
        } else {
            bookings = bookingRepository.findByLenderId(lenderId);
        }
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle PENDING Bookings eines Verleihers als DTOs
     */
    public List<BookingDTO> getPendingBookingsByLenderId(Long lenderId, boolean includeDeleted) {
        List<Booking> bookings;
        if (includeDeleted) {
            bookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getLender() != null && b.getLender().getId().equals(lenderId))
                    .filter(b -> b.calculateStatus() == BookingStatus.PENDING)
                    .toList();
        } else {
            bookings = bookingRepository.findPendingByLenderId(lenderId);
        }
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle überfälligen Bookings als DTOs
     */
    public List<BookingDTO> getOverdueBookings() {
        List<Booking> bookings = bookingRepository.findOverdue(LocalDateTime.now());
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle Bookings mit optionalem Status-Filter
     * Nutzt optimierte Repository-Queries statt Stream-Filterung
     *
     * @param status Optional: "overdue", "pending", "confirmed", "picked_up", "returned", "cancelled", "expired", "rejected"
     * @return Liste von BookingDTOs
     */
    public List<BookingDTO> getAllBookings(String status) {
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold24h = now.minusHours(24);

        if (status == null || status.isBlank()) {
            // Alle Bookings (ohne gelöschte)
            bookings = bookingRepository.findAllActive();
        } else {
            // Nach Status filtern mit spezifischen Queries
            bookings = switch (status.toLowerCase()) {
                case "overdue" -> bookingRepository.findOverdue(now);
                case "pending" -> bookingRepository.findAllPending(threshold24h);
                case "confirmed" -> bookingRepository.findAllConfirmed(threshold24h);
                case "picked_up" -> bookingRepository.findAllPickedUp();
                case "returned" -> bookingRepository.findAllReturned();
                case "cancelled" -> bookingRepository.findAllCancelled(threshold24h);
                case "expired" -> bookingRepository.findAllExpired(threshold24h);
                case "rejected" -> bookingRepository.findAllRejected();
                default -> throw new IllegalArgumentException("Invalid status: " + status +
                        ". Valid values: overdue, pending, confirmed, picked_up, returned, cancelled, expired, rejected");
            };
        }

        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt bestätigte, aber noch nicht abgeholte Buchungen (Zukünftig)
     */
    public List<BookingDTO> getUpcomingBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findUpcomingByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt aktuell ausgeliehene Gegenstände (Aktiv)
     * Sortiert nach Rückgabedatum, damit überfällige oben stehen.
     */
    public List<BookingDTO> getActiveBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findActiveByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt nur die überfälligen Buchungen für einen Verleiher
     */
    public List<BookingDTO> getOverdueBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findOverdueByLenderId(lenderId, LocalDateTime.now());
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

        // Prüfe ob Item existiert und verfügbar ist
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

        // Prüfe ob User existiert
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Prüfe Verfügbarkeit (keine überlappenden Bookings)
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(itemId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Item is not available for the requested period");
        }

        // Hole Verleiher
        User lender = item.getLender();
        if (lender == null) {
            throw new RuntimeException("No lender assigned to this product");
        }

        // Erstelle Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setLender(lender);
        booking.setItem(item);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setMessage(message);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    // ========================================
    // UPDATE METHODEN
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
            throw new RuntimeException("Artikel wurde noch nicht abgeholt.");
        }

        if (booking.getReturnDate() != null) {
            throw new RuntimeException("Artikel wurde bereits zurückgegeben.");
        }

        booking.setReturnDate(LocalDateTime.now());
        log.info("Rückgabe dokumentiert: Buchung ID {}, Item {}, User {}",
                id, booking.getItem().getInvNumber(), booking.getUser().getUniqueId());
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Generische Methode für alle Status-Updates via PATCH-Endpoint
     *
     * @param id Booking ID
     * @param updateDTO DTO mit action und optionalen Parametern
     * @return Aktualisierte Booking als DTO
     */
    @Transactional
    public BookingDTO updateStatus(Long id, BookingStatusUpdateDTO updateDTO) {
        String action = updateDTO.getAction();

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action is required");
        }

        User currentUser = userService.getCurrentUser();

        return switch (action.toLowerCase()) {
            case "confirm" -> {
                if (updateDTO.getProposedPickups() == null || updateDTO.getProposedPickups().isEmpty()) {
                    throw new IllegalArgumentException("proposedPickups is required for action 'confirm'");
                }
                yield confirmBooking(id, updateDTO.getProposedPickups());
            }
            case "select_pickup" -> {
                if (updateDTO.getSelectedPickup() == null) {
                    throw new IllegalArgumentException("selectedPickup is required for action 'select_pickup'");
                }
                yield selectPickupTime(id, updateDTO.getSelectedPickup());
            }
            case "propose" -> {
                if (updateDTO.getProposedPickups() == null || updateDTO.getProposedPickups().isEmpty()) {
                    throw new IllegalArgumentException("proposedPickups is required for action 'propose'");
                }
                yield proposeNewPickups(id, currentUser.getId(), updateDTO.getProposedPickups());
            }
            case "pickup" -> recordPickup(id);
            case "return" -> recordReturn(id);
            default -> throw new IllegalArgumentException(
                    "Invalid action: " + action +
                            ". Valid values: confirm, select_pickup, propose, pickup, return"
            );
        };
    }

    /**
     * Student oder Admin storniert Booking (auch für Verleiher-Reject)
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
    // ABHOL-TOKEN / E-MAIL-ABLAUF
    // ========================================

    /**
     * Der Verleiher löst die E-Mail aus.
     * Erzeugt ein Token und sendet eine E-Mail an den Studenten.
     */
    @Transactional
    public void initiatePickupProcess(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        // Buchung muss BESTÄTIGT sein
        if (booking.calculateStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Abholung kann nur für bestätigte Buchungen gestartet werden.");
        }

        // Token erzeugen
        String token = UUID.randomUUID().toString();

        // Ablaufzeit setzen (15 Min)
        booking.setPickupToken(token);
        booking.setPickupTokenExpiry(LocalDateTime.now().plusMinutes(15));
        bookingRepository.save(booking);

        // Link erzeugen + E-Mail senden
        String link = baseUrl + "/api/bookings/verify-pickup?token=" + token;

        System.out.println("==========================================");
        System.out.println("DEBUG - GENERATED TOKEN: " + token);
        System.out.println("DEBUG - CLICK THIS LINK: " + link);
        System.out.println("==========================================");

        // Falls User noch kein email-Feld hat
        String userEmail = "dev.email@hs-eslingen.de";
        emailService.sendPickupConfirmation(userEmail, link);
    }

    /**
     * Benutzer klickt auf den Link.
     * Validiert das Token und führt die Abholung durch.
     */
    @Transactional
    public BookingDTO verifyPickupToken(String token) {

        //  finde Buchung anhand des Tokens
        Booking booking = bookingRepository.findByPickupToken(token)
                .orElseThrow(() -> new RuntimeException("Ungültiger oder abgelaufener Token."));

        // Gültigkeitsprüfung
        if (booking.getPickupTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token ist abgelaufen. Bitte Verleiher um neuen Link bitten.");
        }

        // Bereits abgeholt?
        if (booking.getDistributionDate() != null) {
            throw new RuntimeException("Artikel wurde bereits abgeholt.");
        }

        // Abholung durchführen
        booking.setDistributionDate(LocalDateTime.now());

        // Token zurücksetzen
        booking.setPickupToken(null);
        booking.setPickupTokenExpiry(null);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
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