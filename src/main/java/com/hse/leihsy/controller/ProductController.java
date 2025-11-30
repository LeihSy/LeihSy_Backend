package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.mapper.ProductMapper;
import com.hse.leihsy.model.dto.ProductCreateDTO;
import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/products", produces = "application/json")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ProductMapper productMapper, ObjectMapper objectMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Get all products", description = "Returns a list of all products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(productMapper.toDTOList(products));
    }

    @Operation(summary = "Get product by ID", description = "Returns a product with matching ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID of the product to retrieve") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    @Operation(summary = "Get products by category", description = "Returns a list of products filtered by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    })
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @Parameter(description = "ID of the category") @PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(productMapper.toDTOList(products));
    }

    @Operation(summary = "Get products by location", description = "Returns a list of products filtered by location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    })
    @GetMapping("/locations/{locationId}")
    public ResponseEntity<List<ProductDTO>> getProductsByLocation(
            @Parameter(description = "ID of the location") @PathVariable Long locationId) {
        List<Product> products = productService.getProductsByLocation(locationId);
        return ResponseEntity.ok(productMapper.toDTOList(products));
    }

    @Operation(summary = "Search products", description = "Search for products using a query string")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    })
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @Parameter(description = "Query string for full-text search") @RequestParam String q) {
        List<Product> products = productService.fullTextSearch(q);
        return ResponseEntity.ok(productMapper.toDTOList(products));
    }

    @Operation(summary = "Create a new product", description = "Creates a new product with the given data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
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

            return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(created));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Operation(summary = "Update a product", description = "Updates an existing product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
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

            return ResponseEntity.ok(productMapper.toDTO(updated));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}