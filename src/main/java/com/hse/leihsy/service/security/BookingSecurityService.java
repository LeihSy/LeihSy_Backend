package com.hse.leihsy.service.security;

import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Security Service für Booking-Autorisierung
 *
 * Prüft ob ein User berechtigt ist, eine Booking zu sehen oder zu ändern.
 */
@Service("bookingSecurityService")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class BookingSecurityService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    /**
     * Prüft ob der aktuelle User eine Booking sehen darf.
     *
     * Erlaubt wenn:
     * - User ist der Entleiher (user)
     * - User ist der Verleiher (lender)
     * - User ist Mitglied der zugeordneten Gruppe (bei Gruppenbuchungen)
     *
     * @param bookingId ID der Booking
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canView(Long bookingId, Authentication authentication) {
        if (authentication == null || bookingId == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();

        // User ist Entleiher
        if (booking.getUser() != null && booking.getUser().getId().equals(currentUser.getId())) {
            return true;
        }

        // User ist Verleiher
        if (booking.getLender() != null && booking.getLender().getId().equals(currentUser.getId())) {
            return true;
        }

        // Bei Gruppenbuchung: User ist Mitglied der Gruppe
        if (booking.getStudentGroup() != null &&
            booking.getStudentGroup().isMember(currentUser)) {
            return true;
        }

        return false;
    }

    /**
     * Prüft ob der aktuelle User eine Booking ändern darf.
     *
     * Erlaubt wenn:
     * - User ist der Verleiher (lender) → kann confirm/reject/pickup/return
     * - User ist der Entleiher (user) → kann select_pickup/propose/cancel
     *
     * @param bookingId ID der Booking
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canUpdate(Long bookingId, Authentication authentication) {
        if (authentication == null || bookingId == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();

        // User ist Entleiher oder Verleiher
        boolean isEntleiher = booking.getUser() != null &&
                              booking.getUser().getId().equals(currentUser.getId());
        boolean isVerleiher = booking.getLender() != null &&
                              booking.getLender().getId().equals(currentUser.getId());

        return isEntleiher || isVerleiher;
    }

    /**
     * Prüft ob der aktuelle User eine Booking löschen/ablehnen darf.
     *
     * Erlaubt wenn:
     * - User ist der Verleiher (lender) → kann ablehnen
     * - User ist der Entleiher (user) → kann stornieren
     *
     * @param bookingId ID der Booking
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canDelete(Long bookingId, Authentication authentication) {
        // Gleiche Logik wie canUpdate
        return canUpdate(bookingId, authentication);
    }

    /**
     * Prüft ob der User alle Bookings eines Lenders sehen darf.
     *
     * Erlaubt wenn:
     * - User ist der Lender selbst
     *
     * @param lenderId ID des Lenders
     * @param authentication Spring Security Authentication
     * @return true wenn User berechtigt ist
     */
    public boolean canViewLenderBookings(Long lenderId, Authentication authentication) {
        if (authentication == null || lenderId == null) {
            return false;
        }

        User currentUser = userService.getCurrentUser();
        return currentUser.getId().equals(lenderId);
    }
}
