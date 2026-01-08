package com.hse.leihsy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingStatusUpdateDTO;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.StudentGroupRepository;
import com.hse.leihsy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final ObjectMapper objectMapper;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final EmailService emailService;
    private final PdfGenerationService pdfService;


    // ========================================
    // GET METHODEN
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
     * Holt alle geloeschten/stornierten Bookings eines Users als DTOs
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
     * Holt alle ueberfaelligen Bookings als DTOs
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
     * Holt bestaetigte, aber noch nicht abgeholte Buchungen (Zukuenftig)
     */
    public List<BookingDTO> getUpcomingBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findUpcomingByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt aktuell ausgeliehene Gegenstaende (Aktiv)
     * Sortiert nach Rueckgabedatum, damit ueberfaellige oben stehen.
     */
    public List<BookingDTO> getActiveBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findActiveByLenderId(lenderId);
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt nur die ueberfaelligen Buchungen fuer einen Verleiher
     */
    public List<BookingDTO> getOverdueBookingsByLenderId(Long lenderId) {
        List<Booking> bookings = bookingRepository.findOverdueByLenderId(lenderId, LocalDateTime.now());
        return bookingMapper.toDTOList(bookings);
    }

    /**
     * Holt alle Bookings einer Studentengruppe
     */
    public List<BookingDTO> getBookingsByGroupId(Long groupId) {
        List<Booking> bookings = bookingRepository.findByStudentGroupId(groupId);
        return bookingMapper.toDTOList(bookings);
    }

    // ========================================
    // PRIVATE ENTITY HELPER
    // ========================================

    /**
     * Holt eine Booking Entity anhand der ID (nur intern nutzen!)
     */
    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    // ========================================
    // CREATE METHODEN
    // ========================================

    /**
     * Erstellt eine neue Booking (Einzelbuchung, Rueckwaertskompatibilitaet)
     */
    @Transactional
    public BookingDTO createBooking(Long userId, Long itemId, LocalDateTime startDate,
                                    LocalDateTime endDate, String message) {
        return createBooking(userId, itemId, startDate, endDate, message, null);
    }

    /**
     * Erstellt eine neue Booking mit optionaler Gruppenzuordnung
     */
    @Transactional
    public BookingDTO createBooking(Long userId, Long itemId, LocalDateTime startDate,
                                    LocalDateTime endDate, String message, Long groupId) {

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

        // Verleiher aus Item holen
        User lender = item.getLender();
        if (lender == null && item.getProduct() != null) {
            lender = item.getLender();
        }
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
        booking.setStatus(BookingStatus.PENDING.name());

        // Gruppenzuordnung falls angegeben
        if (groupId != null) {
            StudentGroup group = studentGroupRepository.findActiveById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

            // Pruefen ob User Mitglied der Gruppe ist
            if (!group.isMember(user)) {
                throw new RuntimeException("User is not a member of the specified group");
            }

            booking.setStudentGroup(group);
            log.info("Gruppenbuchung erstellt: User {} fuer Gruppe '{}', Item {}",
                    user.getName(), group.getName(), item.getInvNumber());
        }

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    // ========================================
    // UPDATE METHODEN & EMAIL LOGIK
    // ========================================

    /**
     * Verleiher bestaetigt Booking und schlaegt Abholtermine vor
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
     * Student waehlt einen der vorgeschlagenen Abholtermine aus -> status CONFIRMED
     * * Trigger: E-Mail an Student (CC Lender)
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

        // --- Email Benachrichtigung (CONFIRMED) ---
        try {
            String studentEmail = getEmailOrFallback(saved.getUser().getEmail());
            String lenderEmail = saved.getLender() != null ? saved.getLender().getEmail() : null;

            String subject = "Buchung Bestätigt: " + saved.getItem().getProduct().getName();
            String body = String.format(
                    "<h3>Hallo %s,</h3>" +
                            "<p>Deine Buchung für <b>%s</b> wurde bestätigt.</p>" +
                            "<p><b>Abholtermin:</b> %s</p>" +
                            "<br><p>Dein LeihSy Team.</p>",
                    saved.getUser().getName(),
                    saved.getItem().getProduct().getName(),
                    selectedPickup.toString().replace("T", " ")
            );

            emailService.sendStatusChangeEmail(studentEmail, lenderEmail, subject, body);
        } catch (Exception e) {
            log.error("Fehler beim Senden der CONFIRMED Email", e);
        }
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
     * Verleiher dokumentiert Ausgabe -> Status wird PICKED_UP
     * Trigger: E-Mail mit PDF an Student (CC Lender)
     */
    @Transactional
    public BookingDTO recordPickup(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getConfirmedPickup() == null) {
            throw new RuntimeException("Noch keine Abholzeit bestätigt");
        }

        if (booking.getDistributionDate() != null) {
            throw new RuntimeException("Artikel bereits abgeholt");
        }

        // DB-Status aktualisieren
        booking.setDistributionDate(LocalDateTime.now());
        booking.updateStatus();
        Booking saved = bookingRepository.save(booking);

        // PDF generieren und E-Mail senden
        try {
            log.info("Generating PDF for Booking ID {}", saved.getId());
            byte[] pdfBytes = pdfService.generateBookingPdf(saved);

            // E-Mails ermitteln (inkl. Fallback/Null-Handling)
            String studentEmail = getEmailOrFallback(saved.getUser().getEmail());
            String lenderEmail = saved.getLender() != null ? saved.getLender().getEmail() : null;
            if (studentEmail == null || studentEmail.isEmpty()) studentEmail = "dev.email@hs-esslingen.de";

            String subject = "Abholung Erfolgreich: " + saved.getItem().getProduct().getName();
            String body = String.format(
                    "<h3>Hallo %s,</h3>" +
                            "<p>Du hast den Artikel <b>%s</b> erfolgreich abgeholt.</p>" +
                            "<p>Im Anhang findest du das Übergabeprotokoll als PDF.</p>" +
                            "<br><p>Dein LeihSy Team</p>",
                    saved.getUser().getName(),
                    saved.getItem().getProduct().getName()
            );

            String filename = "Abholung_" + saved.getId() + ".pdf";

            emailService.sendBookingPdf(studentEmail, lenderEmail, subject, body, pdfBytes, filename);

        } catch (Exception e) {
            log.error("Error sending confirmation email for booking {}", saved.getId(), e);
        }

        return bookingMapper.toDTO(saved);
    }

    /**
     * Verleiher dokumentiert Rueckgabe -> Status wird RETURNED
     * Trigger: E-Mail an Student (CC Lender)
     */
    @Transactional
    public BookingDTO recordReturn(Long id) {
        Booking booking = getBookingById(id);

        // Check: Wurde es abgeholt?
        if (booking.getDistributionDate() == null) {
            throw new RuntimeException("Artikel wurde noch nicht abgeholt.");
        }

        // Check: Bereits zurückgegeben?
        if (booking.getReturnDate() != null) {
            throw new RuntimeException("Artikel wurde bereits zurueckgegeben.");
        }

        // Aktualisiere DB-Status
        booking.setReturnDate(LocalDateTime.now());
        log.info("Rueckgabe dokumentiert: Buchung ID {}, Item {}, User {}",
                id, booking.getItem().getInvNumber(), booking.getUser().getUniqueId());
        Booking saved = bookingRepository.save(booking);


        // --- Email Benachrichtigung (RETURNED) ---
        try {
            String studentEmail = getEmailOrFallback(saved.getUser().getEmail());
            String lenderEmail = saved.getLender() != null ? saved.getLender().getEmail() : null;

            String subject = "Rückgabe Erfolgreich: " + saved.getItem().getProduct().getName();
            String body = String.format(
                    "<h3>Hallo %s,</h3>" +
                            "<p>Die Rückgabe für <b>%s</b> wurde erfolgreich dokumentiert.</p>" +
                            "<p>Dein LeihSy Team</b></p>",
                    saved.getUser().getName(),
                    saved.getItem().getProduct().getName()
            );

            emailService.sendStatusChangeEmail(studentEmail, lenderEmail, subject, body);
        } catch (Exception e) {
            log.error("Fehler beim Senden der RETURNED Email", e);
        }


        return bookingMapper.toDTO(saved);
    }

    /**
     * Statusänderung: ANY -> REJECTED / CANCELLED
     */
    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = getBookingById(id);

        BookingStatus status = booking.calculateStatus();
        if (status == BookingStatus.PICKED_UP || status == BookingStatus.RETURNED) {
            throw new RuntimeException("Cannot cancel a booking that is already picked up or returned");
        }

        // DB Update
        booking.setDeletedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        // EMAIL: REJECTED / CANCELLED
        try {
            String studentEmail = getEmailOrFallback(saved.getUser().getEmail());
            String lenderEmail = saved.getLender() != null ? saved.getLender().getEmail() : null;

            String subject = "Buchung Storniert/Abgelehnt: " + saved.getItem().getProduct().getName();
            String body = String.format(
                    "<h3>Hallo %s,</h3>" +
                            "<p>Deine Buchung für <b>%s</b> wurde storniert oder abgelehnt.</p>" +
                            "<br><p>Dein LeihSy Team</p>",
                    saved.getUser().getName(),
                    saved.getItem().getProduct().getName()
            );

            // Send to Student, CC Lender
            emailService.sendStatusChangeEmail(studentEmail, lenderEmail, subject, body);

        } catch (Exception e) {
            log.error("Fehler beim Senden der REJECTED Email", e);
        }
    }

    /**
     * Generische Methode fuer alle Status-Updates via PATCH-Endpoint
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

    // ========================================
    // HELPER - TEST FALLBACK FUER EMAIL
    // ========================================
    private String getEmailOrFallback(String email) {
        if (email == null || email.isBlank()) {
            // Forces email to your Thunderbird for testing
            return "dev.email@hs-esslingen.de";
        }
        return email;
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