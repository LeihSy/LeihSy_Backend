package com.hse.leihsy.controller;

import com.hse.leihsy.exception.ValidationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.mapper.ItemMapper;
import com.hse.leihsy.mapper.ProductMapper;
import com.hse.leihsy.model.dto.ItemDTO;
import com.hse.leihsy.model.dto.ProductCreateDTO;
import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.dto.timePeriodDTO;
import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.service.ItemService;
import com.hse.leihsy.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products and their items")
public class ProductController {

    private final ProductService productService;
    private final ItemService itemService;
    private final ProductMapper productMapper;
    private final ItemMapper itemMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "Get all products with optional filters",
            description = "Returns a list of all products. Supports filtering by search query, category, and location."
    )
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @Parameter(description = "Search query for full-text search in name and description")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Filter by location ID")
            @RequestParam(required = false) Long locationId
    ) {
        List<Product> products;

        // Priorisierung: search > categoryId > locationId > all
        if (search != null && !search.isBlank()) {
            products = productService.fullTextSearch(search);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
        } else if (locationId != null) {
            products = productService.getProductsByLocation(locationId);
        } else {
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(productMapper.toDTOList(products));
    }

    @Operation(
            summary = "Get product by ID",
            description = "Returns a product with matching ID"
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID of the product to retrieve") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    @Operation(
            summary = "Get all items of a product",
            description = "Returns a list of all physical items (exemplars) belonging to this product"
    )
    @ApiResponse(responseCode = "200", description = "Items retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{productId}/items")
    public ResponseEntity<List<ItemDTO>> getProductItems(
            @Parameter(description = "ID of the product") @PathVariable Long productId
    ) {
        // Prüfen ob Product existiert
        productService.getProductById(productId);

        List<Item> items = itemService.getItemsByProductId(productId);
        return ResponseEntity.ok(itemMapper.toDTOList(items));
    }

    @Operation(
            summary = "Get available / unavailable periods of product",
            description = "Returns a list of time Periods when the specified amount of items of the product are available / unavailable"
    )
    @ApiResponse(responseCode = "200", description = "Time periods retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/{productId}/periods")
    public ResponseEntity<List<timePeriodDTO>> getPeriods(
            @PathVariable Long productId,
            @RequestParam int requiredQuantity,
            @RequestParam String type
    ) {
        // Prüfe ob Produkt existiert
        productService.getProductById(productId);

        List<timePeriodDTO> periods = new ArrayList<>();

        if ("available".equals(type)) {
            periods = productService.getAvailablePeriods(productId, requiredQuantity);
        } else if ("unavailable".equals(type)) {
            periods = productService.getUnavailablePeriods(productId, requiredQuantity);
        }

        return ResponseEntity.ok(periods);
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with the given data"
    )
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ProductCreateDTO createDTO = objectMapper.readValue(productJson, ProductCreateDTO.class);

            // Manuelle Validierung
            validateDTO(createDTO);

            Product product = new Product();
            product.setName(createDTO.getName());
            product.setDescription(createDTO.getDescription());
            product.setExpiryDate(createDTO.getExpiryDate());
            product.setPrice(createDTO.getPrice());
            product.setAccessories(createDTO.getAccessories());
            product.setIsActive(true); // Standardmäßig aktiv

            Product created = productService.createProduct(
                    product,
                    createDTO.getCategoryId(),
                    createDTO.getLocationId(),
                    image
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(created));
        } catch (Exception e) {
            throw new ValidationException("Failed to create product: " + e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Update a product",
            description = "Updates an existing product by ID"
    )
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        try {
            ProductCreateDTO updateDTO = objectMapper.readValue(productJson, ProductCreateDTO.class);

            // Manuelle Validierung
            validateDTO(updateDTO);

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

            return ResponseEntity.ok(productMapper.toDTO(updated));
        } catch (Exception e) {
            throw new ValidationException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by ID (soft-delete)"
    )
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validiert ein DTO manuell und wirft eine ValidationException bei Fehlern
     */
    private void validateDTO(ProductCreateDTO dto) {
        Set<ConstraintViolation<ProductCreateDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + errors);
        }
    }
}