package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemBatchCreateDTO {

    // "Anzahl eingeben"
    @NotNull(message = "Anzahl ist erforderlich")
    @Min(value = 1, message = "Anzahl muss mindestens 1 sein")
    private Integer count;

    // Basis für die "Automatische Nummerierung" (z.B. "VR" wird zu "VR-001")
    @NotBlank(message = "Inventarnummer-Präfix ist erforderlich")
    private String invNumberPrefix;

    @NotNull(message = "Produkt ID ist erforderlich")
    private Long productId;

    private String owner;
}