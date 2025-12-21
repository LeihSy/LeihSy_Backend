package com.hse.leihsy.model.dto;

import java.time.LocalDateTime;

/**
 * DTO für Location - enthält nur die Location-Attribute ohne die verknüpften Produkte
 */
public class LocationDTO {

    private Long id;
    private String roomNr;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LocationDTO() {
    }

    public LocationDTO(Long id, String roomNr, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.roomNr = roomNr;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNr() {
        return roomNr;
    }

    public void setRoomNr(String roomNr) {
        this.roomNr = roomNr;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

