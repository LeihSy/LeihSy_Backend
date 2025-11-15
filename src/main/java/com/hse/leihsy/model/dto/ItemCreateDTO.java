package com.hse.leihsy.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ItemCreateDTO {

    @NotBlank(message = "Inventarnummer ist erforderlich")
    private String inventoryNumber;

    @NotBlank(message = "Name ist erforderlich")
    private String name;

    private String description;

    @NotNull(message = "Kategorie ist erforderlich")
    private Long categoryId;

    private String location;
    private String imageUrl;
    private String accessories;

    // Constructors
    public ItemCreateDTO() {}

    public ItemCreateDTO(String inventoryNumber, String name, String description,
                         Long categoryId, String location, String imageUrl,
                         String accessories) {
        this.inventoryNumber = inventoryNumber;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.location = location;
        this.imageUrl = imageUrl;
        this.accessories = accessories;
    }

    // Getters
    public String getInventoryNumber() {
        return inventoryNumber;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAccessories() {
        return accessories;
    }

    // Setters
    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber = inventoryNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }
}