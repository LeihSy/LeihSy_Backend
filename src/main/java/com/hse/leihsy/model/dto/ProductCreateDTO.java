package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data // Auto-generates getters, setters, toString, etc.
@NoArgsConstructor // Generates empty constructor
@AllArgsConstructor // Generates full constructor

public class ProductCreateDTO {

    @NotBlank(message = "Name ist erforderlich")
    private String name;

    private String description;

    @Positive(message = "Ausleihdauer muss positiv sein")
    private Integer expiryDate;

    @Positive(message = "Preis muss positiv sein")
    private BigDecimal price;

    private String imageUrl;

    private String accessories;

    @NotNull(message = "Kategorie ist erforderlich")
    private Long categoryId;

    @NotNull(message = "Standort ist erforderlich")
    private Long locationId;

    private Long lenderId;
}