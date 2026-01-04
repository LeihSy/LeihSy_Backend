package com.hse.leihsy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;


/**
 * EmailService - Zentraler Service für den E-Mail-Versand
 * Nutzt den JavaMailSender um Nachrichten über den konfigurierten SMTP-Server zu verschicken.
 */

@Service
@RequiredArgsConstructor
@Slf4j

public class EmailService {

    private final JavaMailSender mailSender;

    // Die Absender-Adresse aus der application.properties (app.mail.sender)
    @Value("${app.mail.sender}")
    private String senderEmail;

    /**
     * Sendet eine Bestätigungs-Email zur Abholung an den Studenten.
     * Enthält den Link, der zur Bestätigung geklickt werden muss.
     */
    public void sendPickupConfirmation(String toEmail, String pickupLink) {
        try {

            //  Nachricht erstellen
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            //  Header-Daten setzen (Von, An, Betreff)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("LeihSy: Bestätigung Ihrer Abholung");

            //  Text-Inhalt definieren
            String htmlMsg = String.format(
                    "<html>" +
                    "<body>" +
                    "<h3>Hallo,</h3>" +
                    "<p>Bitte bestaetigen Sie die Abholung Ihres Artikels durch Klicken auf folgenden Link:</p>" +
                    "<p><a href='%s' style='font-size: 16px; color: #1a73e8;'><strong>[ Abholung jetzt bestaetigen ]</strong></a></p>" +
                    "<br>" +
                    "<p><small>Falls der Link nicht funktioniert, kopieren Sie diese URL in Ihren Browser:</small><br>" +
                    "<small>%s</small></p>" +
                    "<p><i>Dieser Link ist 15 Minuten gueltig.</i></p>" +
                    "</body>" +
                    "</html>",
                    pickupLink, pickupLink
            );

            // In HTML umwandeln
            helper.setText(htmlMsg, true);

            // E-Mail versenden
            mailSender.send(mimeMessage);
            log.info("Abholbestätigung gesendet an: {}", toEmail);
        } catch (MessagingException e) {
            // Fehler loggen aber als RuntimeException weiterwerfen
            log.error("Fehler beim Erstellen der HTML-E-Mail an {}", toEmail, e);
            throw new RuntimeException("Email konnte nicht gesendet werden.");
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Senden an {}", toEmail, e);
            throw new RuntimeException("Email-Versand fehlgeschlagen.");
        }
    }
}
