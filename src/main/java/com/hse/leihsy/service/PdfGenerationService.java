package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Booking;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j

public class PdfGenerationService {

    // Einheitliches Datumsformat für alle Zeitangaben im PDF
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246); // Blue-500 equivalent

    /**
     * Erstellt eine PDF-Abholbestätigung für eine Buchung.
     * Rückgabe als Byte-Array, damit es direkt als HTTP-Response/Download versendet werden kann.
     */
    public byte[] generateBookingPdf(Booking booking) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            // 1. Header
            addHeader(document, booking);

            // 2. Booking Info: Buchungsdetails (Status, Zeitpunkte, IDs)
            addSectionTitle(document, "Buchungsinformationen");
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20f);

            addInfoRow(infoTable, "Buchungs-ID:", "#" + booking.getId());
            addInfoRow(infoTable, "Status:", booking.getStatus()); // Or translate status
            addInfoRow(infoTable, "Erstellt am:", booking.getCreatedAt().format(DATE_FORMATTER));
            if(booking.getDistributionDate() != null) {
                addInfoRow(infoTable, "Ausleihdatum:", booking.getDistributionDate().format(DATE_FORMATTER));
            }
            document.add(infoTable);

            // 3. User Info (Entleiher)
            addSectionTitle(document, "Entleiher");
            PdfPTable userTable = new PdfPTable(2);
            userTable.setWidthPercentage(100);
            userTable.setSpacingAfter(20f);

            addInfoRow(userTable, "Name:", booking.getUser().getName());
            addInfoRow(userTable, "User-ID:", booking.getUser().getUniqueId());
            document.add(userTable);

            // 4. Item Info: Gegenstand/Inventar (was genau ausgeliehen wird + Verleiher)
            addSectionTitle(document, "Gegenstand");
            PdfPTable itemTable = new PdfPTable(2);
            itemTable.setWidthPercentage(100);
            itemTable.setSpacingAfter(20f);

            addInfoRow(itemTable, "Produkt:", booking.getItem().getProduct().getName());
            addInfoRow(itemTable, "Inventarnummer:", booking.getItem().getInvNumber());
            addInfoRow(itemTable, "Verleiher:", booking.getLender() != null ? booking.getLender().getName() : "N/A");
            document.add(itemTable);

            // Footer
            addFooter(document);

            document.close();
            return out.toByteArray();

        } catch (DocumentException | IOException e) {
            log.error("Error creating PDF", e);
            throw new RuntimeException("Could not generate PDF", e);
        }
    }

    // Erstellt den visuellen Kopfbereich mit farbigem Hintergrund und Titel
    private void addHeader(Document document, Booking booking) throws DocumentException {
        // Blue Box Header simulation
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(PRIMARY_COLOR);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(20f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.WHITE);
        Paragraph title = new Paragraph("Abholbestätigung", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(title);
        headerTable.addCell(cell);

        document.add(headerTable);
        document.add(new Paragraph(" ")); // Spacer
    }

    private void addSectionTitle(Document document, String text) throws DocumentException {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Paragraph p = new Paragraph(text, font);
        p.setSpacingAfter(10f);
        document.add(p);

        // Line separator
        com.lowagie.text.pdf.draw.LineSeparator line = new com.lowagie.text.pdf.draw.LineSeparator();
        line.setLineColor(Color.LIGHT_GRAY);
        document.add(line);
        document.add(new Paragraph(" "));
    }

    /**
     * Fügt eine Abschnittsüberschrift inkl. Line separator ein,
     * um Inhalte im PDF klar zu strukturieren.
     */
    private void addInfoRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Paragraph p = new Paragraph("Generiert von LeihSy - Ausleihsystem", footerFont);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(30f);
        document.add(p);
    }
}
