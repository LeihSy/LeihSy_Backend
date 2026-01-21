package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO für das Erstellen einer neuen Location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationCreateDTO {

    @NotBlank(message = "Room number is required")
    @Size(max = 50, message = "Room number must not exceed 50 characters")
    private String roomNr;
}
