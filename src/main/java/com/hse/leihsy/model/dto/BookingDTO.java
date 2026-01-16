package com.hse.leihsy.model.dto;

import com.hse.leihsy.model.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private String roomNr;
    private Long id;
    private Long userId;
    private String userName;
    private Long lenderId;
    private String lenderName;
    private Long itemId;
    private String itemInvNumber;
    private Long productId;
    private String productName;
    private Long proposalById;
    private String proposalByName;
    private String message;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String proposedPickups;
    private LocalDateTime confirmedPickup;
    private LocalDateTime distributionDate;
    private LocalDateTime returnDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean urgent; // für Dringlichkeit (nahe der 24h Frist)
    private boolean overdue; // Abgeholt – Rueckgabedatum überschritten
    // Gruppen-Infos (NULL wenn Einzelbuchung)
    private Long groupId;
    private String groupName;
    private List<String> groupMemberNames; // Namen aller Gruppenmitglieder
}