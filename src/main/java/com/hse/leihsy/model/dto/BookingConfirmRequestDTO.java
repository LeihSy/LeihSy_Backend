package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

public class BookingConfirmRequestDTO {

    @NotEmpty(message = "At least one proposed pickup time is required")
    private List<LocalDateTime> proposedPickups;

    public BookingConfirmRequestDTO() {
    }

    public List<LocalDateTime> getProposedPickups() {
        return proposedPickups;
    }

    public void setProposedPickups(List<LocalDateTime> proposedPickups) {
        this.proposedPickups = proposedPickups;
    }
}