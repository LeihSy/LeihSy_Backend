package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BookingSelectPickupRequestDTO {

    @NotNull(message = "Selected pickup time is required")
    private LocalDateTime selectedPickup;

    public BookingSelectPickupRequestDTO() {
    }

    public LocalDateTime getSelectedPickup() {
        return selectedPickup;
    }

    public void setSelectedPickup(LocalDateTime selectedPickup) {
        this.selectedPickup = selectedPickup;
    }
}