package com.hse.leihsy.controller;

import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


import java.util.List;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

   /** public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    **/


    @Operation(summary = "Get all categories", description = "Returns a list of all active categories")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAllActive();
        return ResponseEntity.ok(categories);
    }


    @Operation(summary = "Get category by ID", description = "Returns a category with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + id));
        return ResponseEntity.ok(category);
    }
}