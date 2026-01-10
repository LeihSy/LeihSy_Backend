package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;
import lombok.*;

/**
 * DTO für Location - enthält nur die Location-Attribute ohne die verknüpften Produkte
 */
@Data
@NoArgsConstructor
@Builder
public class LocationDTO {

    private Long id;
    private String roomNr;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocationDTO(Long id, String roomNr, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roomNr = roomNr;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


}

