package com.hse.leihsy.service;

import com.hse.leihsy.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public String saveImage(MultipartFile file, String productName) {
        validateImage(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // Produktname zu URL-freundlichem Dateinamen konvertieren
        String sanitizedName = productName
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")  // Nur Buchstaben, Zahlen, Rest wird zu "-"
                .replaceAll("^-+|-+$", "");      // Führende/Trailing "-" entfernen

        String filename = sanitizedName + "." + extension;

        try {
            Path filepath = Paths.get(uploadDir, filename);
            Files.createDirectories(filepath.getParent());

            // Altes Bild mit gleichem Namen löschen falls vorhanden
            Files.deleteIfExists(filepath);

            Files.write(filepath, file.getBytes());

            log.info("Image saved successfully: {}", filename);
            return filename;
        } catch (IOException e) {
            log.error("Failed to save image: {}", originalFilename, e);
            throw new FileStorageException("Failed to save image", e);
        }
    }

    public Resource loadImage(String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filename);
            }
        } catch (IOException e) {
            log.error("Failed to load image: {}", filename, e);
            throw new FileStorageException("Failed to load image: " + filename, e);
        }
    }

    public void deleteImage(String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(file);
            log.info("Image deleted successfully: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", filename, e);
            throw new FileStorageException("Failed to delete image: " + filename, e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds maximum allowed size of 5MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new FileStorageException("File name is invalid");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS)
            );
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileStorageException("File has no extension");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}