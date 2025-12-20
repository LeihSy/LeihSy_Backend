package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.CategoryMapper;
import com.hse.leihsy.model.dto.CategoryDTO;
import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Operation(
            summary = "Get all categories",
            description = "Returns a list of all active categories"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findAllActive();
        List<CategoryDTO> categoryDTOs = categoryMapper.toDTOList(categories);
        return ResponseEntity.ok(categoryDTOs);
    }

    @Operation(
            summary = "Get category by ID",
            description = "Returns a category with the specified ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + id));
        CategoryDTO categoryDTO = categoryMapper.toDTO(category);
        return ResponseEntity.ok(categoryDTO);
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new product category with the specified name"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Category created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Category.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Category name is required\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<?> createCategory(
            @Parameter(
                    description = "Category data with name",
                    required = true,
                    schema = @Schema(example = "{\"name\": \"Audio-Equipment\"}")
            )
            @RequestBody Map<String, String> request
    ) {
        String name = request.get("name");

        if (name == null || name.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Category name is required"));
        }

        Category category = new Category();
        category.setName(name);

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    @Operation(
            summary = "Delete a category",
            description = "Deletes a category if no products are assigned to it. Uses soft-delete (sets deletedAt timestamp)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Category deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Category has assigned products and cannot be deleted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Cannot delete category: 5 products are still assigned to this category\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable Long id
    ) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + id));

        // Prüfen ob Products existieren
        long productCount = productRepository.countByCategoryId(id);

        if (productCount > 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error",
                            String.format("Cannot delete category: %d products are still assigned to this category", productCount)
                    ));
        }

        // Soft-Delete
        category.softDelete();
        categoryRepository.save(category);

        return ResponseEntity.noContent().build();
    }
}