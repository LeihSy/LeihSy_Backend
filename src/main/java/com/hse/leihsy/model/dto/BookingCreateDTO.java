package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

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

    public BookingCreateDTO() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getProposalPickup() {
        return proposalPickup;
    }

    public void setProposalPickup(LocalDateTime proposalPickup) {
        this.proposalPickup = proposalPickup;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}