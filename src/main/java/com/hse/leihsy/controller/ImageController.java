package com.hse.leihsy.controller;

import com.hse.leihsy.exception.FileStorageException;
import com.hse.leihsy.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Tag(name = "Image Management", description = "APIs for uploading, retrieving, and deleting product images")
public class ImageController {

    private final ImageService imageService;

    @Operation(
            summary = "Upload a product image",
            description = "Uploads an image file for a product. The image will be saved with a sanitized filename based on the product name. Supported formats: JPG, PNG, WebP (max 5MB)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"imageUrl\": \"/api/images/meta-quest-pro.jpg\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file (wrong format, too large, or empty)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"File size exceeds maximum allowed size of 5MB\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during upload",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Failed to upload image\"}")
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(
                    description = "Image file to upload (JPG, PNG, or WebP, max 5MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(
                    description = "Product name for generating the filename (e.g., 'Meta Quest Pro' → 'meta-quest-pro.jpg'). If not provided, original filename will be used.",
                    required = false,
                    example = "Meta Quest Pro"
            )
            @RequestParam(value = "productName", required = false) String productName
    ) {
        try {
            // Fallback: Wenn kein productName, dann Original-Filename verwenden
            String nameToUse = (productName != null && !productName.isBlank())
                    ? productName
                    : file.getOriginalFilename().replaceFirst("[.][^.]+$", ""); // Ohne Extension

            String filename = imageService.saveImage(file, nameToUse);
            String imageUrl = "/api/images/" + filename;

            log.info("Image uploaded successfully: {}", filename);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (FileStorageException e) {
            log.error("Failed to upload image", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during image upload", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }

    @Operation(
            summary = "Get a product image",
            description = "Retrieves an image file by its filename. The image is returned inline for display in browsers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image found and returned",
                    content = @Content(
                            mediaType = "image/jpeg"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found"
            )
    })
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(
            @Parameter(
                    description = "Filename of the image to retrieve (e.g., 'meta-quest-pro.jpg')",
                    required = true,
                    example = "meta-quest-pro.jpg"
            )
            @PathVariable String filename
    ) {
        try {
            Resource resource = imageService.loadImage(filename);

            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (FileStorageException e) {
            log.error("Image not found: {}", filename);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Delete a product image",
            description = "Deletes an image file from the server. This operation cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Image deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Image not found"
            )
    })
    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> deleteImage(
            @Parameter(
                    description = "Filename of the image to delete",
                    required = true,
                    example = "meta-quest-pro.jpg"
            )
            @PathVariable String filename
    ) {
        try {
            imageService.deleteImage(filename);
            log.info("Image deleted successfully: {}", filename);
            return ResponseEntity.noContent().build();
        } catch (FileStorageException e) {
            log.error("Failed to delete image: {}", filename);
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}