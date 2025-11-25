package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.model.entity.BookingStatus;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    // Booking per ID abrufen
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking nicht gefunden: " + id));
    }

    // Buchungen eines Users (als Entleiher)
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    // Buchungen für einen Verleiher
    public List<Booking> getBookingsByReceiver(Long receiverId) {
        return bookingRepository.findByReceiverId(receiverId);
    }

    // Offene Anfragen für einen Verleiher
    public List<Booking> getPendingBookingsForReceiver(Long receiverId) {
        return bookingRepository.findPendingByReceiverId(receiverId);
    }

    // Buchungen nach Status
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(status);
    }

    // Fällige Buchungen (Erinnerung)
    public List<Booking> getBookingsDueSoon(int daysAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderDate = now.plusDays(daysAhead);
        return bookingRepository.findDueSoon(now, reminderDate);
    }

    // Überfällige Buchungen
    public List<Booking> getOverdueBookings() {
        return bookingRepository.findOverdue(LocalDateTime.now());
    }

    // Neue Buchung erstellen
    public Booking createBooking(Long userId, Long itemId, LocalDateTime startDate,
                                 LocalDateTime endDate, LocalDateTime proposalPickup, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden: " + userId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden: " + itemId));

        // Prüfe Verfuegbarkeit
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(itemId, startDate, endDate);
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Item ist im gewünschten Zeitraum nicht verfügbar");
        }

        Booking booking = new Booking(user, item, startDate, endDate);
        booking.setProposalPickup(proposalPickup);
        booking.setProposalBy(user);
        booking.setMessage(message);
        booking.setStatus(BookingStatus.PENDING.name());

        return bookingRepository.save(booking);
    }

    // Buchung bestätigen (Verleiher akzeptiert)
    public Booking confirmBooking(Long bookingId, LocalDateTime confirmedPickup) {
        Booking booking = getBookingById(bookingId);

        if (!BookingStatus.PENDING.name().equals(booking.getStatus())) {
            throw new RuntimeException("Buchung kann nicht bestätigt werden. Status: " + booking.getStatus());
        }

        booking.setConfirmedPickup(confirmedPickup);
        booking.setStatus(BookingStatus.CONFIRMED.name());
        return bookingRepository.save(booking);
    }

    // Gegenvorschlag machen
    public Booking proposeNewPickup(Long bookingId, Long proposerId, LocalDateTime newProposal) {
        Booking booking = getBookingById(bookingId);
        User proposer = userRepository.findById(proposerId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden: " + proposerId));

        booking.setProposalPickup(newProposal);
        booking.setProposalBy(proposer);
        return bookingRepository.save(booking);
    }

    // Buchung ablehnen
    public Booking rejectBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(BookingStatus.REJECTED.name());
        booking.softDelete();
        return bookingRepository.save(booking);
    }

    // Ausgabe dokumentieren
    public Booking recordPickup(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (!BookingStatus.CONFIRMED.name().equals(booking.getStatus())) {
            throw new RuntimeException("Ausgabe nicht möglich. Status: " + booking.getStatus());
        }

        booking.setDistributionDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PICKED_UP.name());
        return bookingRepository.save(booking);
    }

    // Rückgabe dokumentieren
    public Booking recordReturn(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (!BookingStatus.PICKED_UP.name().equals(booking.getStatus())) {
            throw new RuntimeException("Rückgabe nicht moeglich. Status: " + booking.getStatus());
        }

        booking.setReturnDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.RETURNED.name());
        return bookingRepository.save(booking);
    }

    // Buchung stornieren (durch User)
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);

        if (BookingStatus.PICKED_UP.name().equals(booking.getStatus())) {
            throw new RuntimeException("Aktive Ausleihe kann nicht storniert werden");
        }

        booking.setStatus(BookingStatus.CANCELLED.name());
        booking.softDelete();
        return bookingRepository.save(booking);
    }
}