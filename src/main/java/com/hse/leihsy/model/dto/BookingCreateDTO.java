package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateDTO {

    @NotNull(message = "Product ist erforderlich")
    private Long productId;

    @NotNull(message = "Startdatum ist erforderlich")
    private LocalDateTime startDate;

    @NotNull(message = "Enddatum ist erforderlich")
    private LocalDateTime endDate;

    @NotNull(message = "Abholtermin ist erforderlich")
    private LocalDateTime proposalPickup;

    private String message;
}