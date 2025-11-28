package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.mapper.ProductMapper;
import com.hse.leihsy.model.dto.ProductCreateDTO;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;


    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(productMapper.toDTOs(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(productMapper.toDTOs(products));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<ProductDTO>> getProductsByLocation(@PathVariable Long locationId) {
        List<Product> products = productService.getProductsByLocation(locationId);
        return ResponseEntity.ok(productMapper.toDTOs(products));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        List<Product> products = productService.fullTextSearch(q);
        return ResponseEntity.ok(productMapper.toDTOs(products));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO createDTO) {
        Product product = new Product();
        product.setName(createDTO.getName());
        product.setDescription(createDTO.getDescription());
        product.setExpiryDate(createDTO.getExpiryDate());
        product.setPrice(createDTO.getPrice());
        product.setImageUrl(createDTO.getImageUrl());
        product.setAccessories(createDTO.getAccessories());

        Product created = productService.createProduct(
                product,
                createDTO.getCategoryId(),
                createDTO.getLocationId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductCreateDTO updateDTO) {

        Product product = new Product();
        product.setName(updateDTO.getName());
        product.setDescription(updateDTO.getDescription());
        product.setExpiryDate(updateDTO.getExpiryDate());
        product.setPrice(updateDTO.getPrice());
        product.setImageUrl(updateDTO.getImageUrl());
        product.setAccessories(updateDTO.getAccessories());

        Product updated = productService.updateProduct(
                id,
                product,
                updateDTO.getCategoryId(),
                updateDTO.getLocationId()
        );

        return ResponseEntity.ok(productMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}