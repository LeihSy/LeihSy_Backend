package com.hse.leihsy.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO für StudentGroup Responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentGroupDTO {

    private Long id;
    private String name;
    private String description;

    // Ersteller-Infos
    private Long createdById;
    private String createdByName;

    // Budget (für später)
    private BigDecimal budget;

    // Mitglieder als Liste von Mini-DTOs
    private List<GroupMemberDTO> members;

    // Statistiken
    private int memberCount;
    private int activeBookingsCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Mini-DTO für Gruppenmitglieder
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupMemberDTO {
        private Long userId;
        private String userName;
        private String userEmail;
        private boolean isOwner;
    }
}