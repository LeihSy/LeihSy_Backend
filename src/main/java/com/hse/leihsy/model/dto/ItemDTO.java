package com.hse.leihsy.model.dto;

import com.hse.leihsy.model.entity.ItemStatus;

public class ItemDTO {
    private Long id;
    private String inventoryNumber;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String location;
    private String imageUrl;
    private ItemStatus status;
    private String accessories;

    // Constructors
    public ItemDTO() {}

    public ItemDTO(Long id, String inventoryNumber, String name, String description,
                   Long categoryId, String categoryName, String location,
                   String imageUrl, ItemStatus status, String accessories) {
        this.id = id;
        this.inventoryNumber = inventoryNumber;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.location = location;
        this.imageUrl = imageUrl;
        this.status = status;
        this.accessories = accessories;
    }

    // Getters
    public Long getId() {
        return id;
    }

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

    public String getCategoryName() {
        return categoryName;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public String getAccessories() {
        return accessories;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

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

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public void setAccessories(String accessories) {
        this.accessories = accessories;
    }
}