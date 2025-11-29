package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.dto.ProductCreateDTO;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(convertToDTO(product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<ProductDTO>> getProductsByLocation(@PathVariable Long locationId) {
        List<Product> products = productService.getProductsByLocation(locationId);
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.fullTextSearch(q);
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ProductCreateDTO createDTO = objectMapper.readValue(productJson, ProductCreateDTO.class);

            Product product = new Product();
            product.setName(createDTO.getName());
            product.setDescription(createDTO.getDescription());
            product.setExpiryDate(createDTO.getExpiryDate());
            product.setPrice(createDTO.getPrice());
            product.setAccessories(createDTO.getAccessories());

            Product created = productService.createProduct(
                    product,
                    createDTO.getCategoryId(),
                    createDTO.getLocationId(),
                    image
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(created));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ProductCreateDTO updateDTO = objectMapper.readValue(productJson, ProductCreateDTO.class);

            Product product = new Product();
            product.setName(updateDTO.getName());
            product.setDescription(updateDTO.getDescription());
            product.setExpiryDate(updateDTO.getExpiryDate());
            product.setPrice(updateDTO.getPrice());
            product.setAccessories(updateDTO.getAccessories());

            Product updated = productService.updateProduct(
                    id,
                    product,
                    updateDTO.getCategoryId(),
                    updateDTO.getLocationId(),
                    image
            );

            return ResponseEntity.ok(convertToDTO(updated));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setExpiryDate(product.getExpiryDate());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setAccessories(product.getAccessories());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getLocation() != null) {
            dto.setLocationId(product.getLocation().getId());
            dto.setLocationRoomNr(product.getLocation().getRoomNr());
        }

        if (product.getLender() != null) {
            dto.setLenderId(product.getLender().getId());
            dto.setLenderName(product.getLender().getName());
        }

        dto.setAvailableItems(product.getAvailableItemCount());
        dto.setTotalItems(product.getTotalItemCount());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        return dto;
    }
}