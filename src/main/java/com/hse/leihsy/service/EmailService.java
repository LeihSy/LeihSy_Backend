package com.hse.leihsy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;

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
     * Sendet eine E-Mail mit einem PDF-Anhang
     */
    public void sendBookingPdf(String to, String cc, String subject, String body, byte[] pdfBytes, String filename) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // true = multipart (needed for attachments)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc);
            }
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            // Attach PDF
            helper.addAttachment(filename, new ByteArrayResource(pdfBytes));

            mailSender.send(mimeMessage);
            log.info("PDF E-Mail erfolgreich gesendet an: {}", to);

        } catch (MessagingException e) {
            log.error("Senden der PDF E-Mail fehlgeschlagen an: {}", to, e);
        }
    }

    /**
     * Sendet eine einfache HTML-Email für Statusänderungen (ohne PDF)
     */
    public void sendStatusChangeEmail(String to, String cc, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(senderEmail);
            helper.setTo(to);

            // WICHTIG: Lender ins CC setzen
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc);
            }

            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("Status-Update Email gesendet an: {} (CC: {})", to, cc);

        } catch (MessagingException e) {
            log.error("Fehler beim Senden der Status-Email an {}", to, e);
        }
    }
}
