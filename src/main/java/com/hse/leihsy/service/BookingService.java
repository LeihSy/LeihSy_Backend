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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
            bookings = bookingRepository.findAllActive();
        } else {
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
    // GET METHODEN - ENTITIES (fuer interne Nutzung)
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
    public List<BookingDTO> createBooking(Long userId, Long productId, LocalDateTime startDate,
                                    LocalDateTime endDate, String message, int quantity) {
        return createBooking(userId, productId, startDate, endDate, message, quantity, null);
    }

    /**
     * Erstellt eine neue Booking mit optionaler Gruppenzuordnung
     */
    @Transactional
    public List<BookingDTO> createBooking(Long userId, Long productId, LocalDateTime startDate,
                                    LocalDateTime endDate, String message, int quantity, Long groupId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        List<Item> itemsOfProduct = itemRepository.findByProductId(productId);


        List<Item> availableItems = new ArrayList<>();
        for(Item item : itemsOfProduct) {
            // Verfuegbarkeitsprüfung
            List<Booking> overlapping = bookingRepository.findOverlappingBookings(item.getId(), startDate, endDate);
            if (overlapping.isEmpty()) {
                availableItems.add(item);
            }
            // Wenn ausreichend verfügbare Items ausgewählt wurden
            if(availableItems.size() >= quantity) {
                break;
            }
        }

        // Prüfe ob genug Items verfügbar sind
        if(availableItems.size() >= quantity) {
            List<Booking> bookings = new ArrayList<>();

            for(Item item : availableItems) {
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
                bookings.add(saved);
            }
            return bookingMapper.toDTOList(bookings);
        } else {
            throw new RuntimeException("Not enough items available for product " + productId);
        }
    }

    // ========================================
    // UPDATE METHODEN
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
     * Student waehlt einen der vorgeschlagenen Abholtermine aus
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
     * Verleiher dokumentiert Rueckgabe
     */
    @Transactional
    public BookingDTO recordReturn(Long id) {
        Booking booking = getBookingById(id);

        if (booking.getDistributionDate() == null) {
            throw new RuntimeException("Artikel wurde noch nicht abgeholt.");
        }

        if (booking.getReturnDate() != null) {
            throw new RuntimeException("Artikel wurde bereits zurueckgegeben.");
        }

        booking.setReturnDate(LocalDateTime.now());
        log.info("Rueckgabe dokumentiert: Buchung ID {}, Item {}, User {}",
                id, booking.getItem().getInvNumber(), booking.getUser().getUniqueId());
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toDTO(saved);
    }

    /**
     * Generische Methode fuer alle Status-Updates via PATCH-Endpoint
     *
     * @param id        Booking ID
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

        BookingDTO result = switch (action.toLowerCase()) {
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

        // Optional: Update message if provided
        if (updateDTO.getMessage() != null) {
            Booking booking = getBookingById(id);
            booking.setMessage(updateDTO.getMessage());
            bookingRepository.save(booking);
            result = bookingMapper.toDTO(booking);
        }

        return result;
    }

    /**
     * Student oder Admin storniert Booking (auch fuer Verleiher-Reject)
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
            return objectMapper.readValue(json, new TypeReference<List<LocalDateTime>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse pickups from JSON", e);
        }
    }
}