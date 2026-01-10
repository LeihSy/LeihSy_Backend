package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service für den Versand von Erinnerungs-Emails.
 * Behandelt "Due Soon" (Fälligkeit in Kürze) und "Overdue" (Überfälligkeit).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Value("${leihsy.reminder.due-soon-days:2}")
    private int dueSoonDays;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Hauptmethode, die vom Scheduler aufgerufen wird.
     */
    @Transactional(readOnly = true)
    public void processReminders() {
        log.info("Starting reminder process...");
        sendDueSoonReminders();
        sendOverdueReminders();
        log.info("Reminder process finished.");
    }

    /**
     * Versendet Erinnerungen für Buchungen, die in X Tagen fällig sind.
     * Standard: 2 Tage vor Rückgabe.
     */
    private void sendDueSoonReminders() {
        LocalDateTime now = LocalDateTime.now();
        // Definieren des Zeitfensters für "in 2 Tagen"
        LocalDateTime reminderLimit = now.plusDays(dueSoonDays);

        log.info("Checking for bookings due soon (within {} days, until {})", dueSoonDays, reminderLimit);

        List<Booking> dueBookings = bookingRepository.findDueSoon(now, reminderLimit);

        // filtern zusätzlich, um nur die zu erwischen, die wirklich am "Ziel-Tag" fällig sind,
        // damit wir nicht jeden Tag diesselbe Mail senden, falls das Intervall größer ist.

        int count = 0;
        for (Booking booking : dueBookings) {
            // Checken ob der Rückgabetermin wirklich genau HEUTE + 2 Tage ist (auf Tag genau)
            long daysUntilDue = ChronoUnit.DAYS.between(now.toLocalDate(), booking.getEndDate().toLocalDate());

            if (daysUntilDue == dueSoonDays) {
                sendReminderEmail(booking, "Erinnerung: Leihfrist endet bald", "due_soon");
                count++;
            }
        }
        log.info("Sent {} 'due soon' reminders.", count);
    }

    /**
     * Versendet Mahnungen für überfällige Buchungen.
     * 1. Tag nach Fälligkeit.
     * Danach wöchentlich.
     */
    private void sendOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> overdueBookings = bookingRepository.findOverdue(now);

        int count = 0;
        for (Booking booking : overdueBookings) {
            long daysOverdue = ChronoUnit.DAYS.between(booking.getEndDate().toLocalDate(), now.toLocalDate());

            // 1 Tag nach Fälligkeit
            if (daysOverdue == 1) {
                sendReminderEmail(booking, "MAHNUNG: Rückgabe überfällig", "overdue_initial");
                count++;
            }
            // wöchentlich (Tag 8, 15, 22...)
            else if (daysOverdue > 1 && (daysOverdue - 1) % 7 == 0) {
                sendReminderEmail(booking, "MAHNUNG: Rückgabe weiterhin überfällig", "overdue_weekly");
                count++;
            }
        }
        log.info("Sent {} overdue reminders.", count);
    }

    private void sendReminderEmail(Booking booking, String subject, String type) {
        if (booking.getUser() == null) {
            log.warn("Cannot send email for booking {}: User has no email", booking.getId());
            return;
        }

        String to = getEmailOrFallback(booking.getUser().getEmail());
        String cc = null;
        if (booking.getLender() != null && booking.getLender().getEmail() != null) {
            cc = booking.getLender().getEmail();
        }

        String itemTitle = "Unbekanntes Item";
        if (booking.getItem() != null) {
            if (booking.getItem().getProduct() != null) {
                itemTitle = booking.getItem().getProduct().getName();
            }
            if (booking.getItem().getInvNumber() != null) {
                itemTitle += " (" + booking.getItem().getInvNumber() + ")";
            }
        }
        String endDateStr = booking.getEndDate().format(DATE_FORMATTER);
        String userName = booking.getUser().getName();

        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h3>Hallo ").append(userName).append(",</h3>");

        if ("due_soon".equals(type)) {
            body.append("<p>Dies ist eine Erinnerung, dass die Leihfrist für folgenden Artikel bald endet:</p>");
        } else {
            body.append(
                    "<p style='color:red;'><strong>Die Leihfrist für folgenden Artikel ist abgelaufen:</strong></p>");
        }

        body.append("<ul>");
        body.append("<li><strong>Artikel:</strong> ").append(itemTitle).append("</li>");
        body.append("<li><strong>Rückgabedatum:</strong> ").append(endDateStr).append("</li>");
        body.append("</ul>");

        body.append("<p>Bitte bringen Sie den Artikel rechtzeitig zurück.</p>");
        body.append("<p>Mit freundlichen Grüßen<br/>Ihr LeihSy Team</p>");
        body.append("</body></html>");

        emailService.sendStatusChangeEmail(to, cc, subject, body.toString());
    }
    // Helper method for fallback email
    private String getEmailOrFallback(String email) {
        if (email == null || email.isBlank()) {
            return "dev.email@hs-esslingen.de";
        }
        return email;
    }
}
