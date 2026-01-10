package com.hse.leihsy.scheduler;

import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler fuer automatische Booking-Stornierungen.
 *
 * Auto-Cancel: Bookings die laenger als X Stunden PENDING sind werden storniert.
 * Auto-Expire: Bookings die laenger als X Stunden CONFIRMED sind ohne Abholung werden expired.
 */
@Component
@ConditionalOnProperty(name = "leihsy.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final com.hse.leihsy.service.ReminderService reminderService;

    @Value("${leihsy.booking.auto-cancel-hours:24}")
    private int autoCancelHours;

    @Value("${leihsy.booking.auto-expire-hours:24}")
    private int autoExpireHours;

    /**
     * Storniert automatisch Bookings die laenger als X Stunden PENDING sind.
     * PENDING = proposedPickups ist NULL (Verleiher hat noch nicht reagiert)
     *
     * Laeuft stuendlich zur vollen Stunde.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoCancelPendingBookings() {
        log.info("Running scheduled task: autoCancelPendingBookings (threshold: {} hours)", autoCancelHours);

        LocalDateTime threshold = LocalDateTime.now().minusHours(autoCancelHours);
        List<Booking> pendingBookings = bookingRepository.findPendingOlderThan(threshold);

        if (pendingBookings.isEmpty()) {
            log.info("No pending bookings to auto-cancel");
            return;
        }

        for (Booking booking : pendingBookings) {
            booking.setDeletedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            log.info("Auto-cancelled booking ID {} (created: {}, user: {}, item: {})",
                    booking.getId(),
                    booking.getCreatedAt(),
                    booking.getUser() != null ? booking.getUser().getName() : "unknown",
                    booking.getItem() != null ? booking.getItem().getInvNumber() : "unknown");
        }

        log.info("Auto-cancelled {} pending bookings", pendingBookings.size());
    }

    /**
     * Markiert automatisch Bookings als EXPIRED wenn X Stunden nach confirmedPickup nicht abgeholt.
     * EXPIRED = confirmedPickup gesetzt, aber distributionDate ist NULL
     *
     * Laeuft stuendlich um :30
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void autoExpireConfirmedBookings() {
        log.info("Running scheduled task: autoExpireConfirmedBookings (threshold: {} hours)", autoExpireHours);

        LocalDateTime threshold = LocalDateTime.now().minusHours(autoExpireHours);
        List<Booking> confirmedBookings = bookingRepository.findConfirmedNotPickedUpOlderThan(threshold);

        if (confirmedBookings.isEmpty()) {
            log.info("No confirmed bookings to auto-expire");
            return;
        }

        for (Booking booking : confirmedBookings) {
            booking.setDeletedAt(LocalDateTime.now());
            bookingRepository.save(booking);

            log.info("Auto-expired booking ID {} (confirmed pickup: {}, user: {}, item: {})",
                    booking.getId(),
                    booking.getConfirmedPickup(),
                    booking.getUser() != null ? booking.getUser().getName() : "unknown",
                    booking.getItem() != null ? booking.getItem().getInvNumber() : "unknown");
        }

        log.info("Auto-expired {} confirmed bookings", confirmedBookings.size());
    }

    /**
     * Versendet tägliche Erinnerungs-Emails (Fälligkeit & Überfälligkeit).
     * Läuft täglich um 08:00 Uhr.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void runReminders() {
        reminderService.processReminders();
    }
}