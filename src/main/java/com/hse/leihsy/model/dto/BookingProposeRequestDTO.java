package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class BookingProposeRequestDTO {

    @NotNull(message = "Proposer ID is required")
    private Long proposerId;

    @NotEmpty(message = "At least one proposed pickup time is required")
    private List<LocalDateTime> proposedPickups;

    public BookingProposeRequestDTO() {
    }

    public Long getProposerId() {
        return proposerId;
    }

    public void setProposerId(Long proposerId) {
        this.proposerId = proposerId;
    }

    public List<LocalDateTime> getProposedPickups() {
        return proposedPickups;
    }

    public void setProposedPickups(List<LocalDateTime> proposedPickups) {
        this.proposedPickups = proposedPickups;
    }
}