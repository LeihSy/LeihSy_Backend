package com.hse.leihsy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class FileStorageConfig {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            log.info("Upload directory initialized: {}", uploadDir);
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }
}