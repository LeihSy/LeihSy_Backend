package com.hse.leihsy.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for custom exception classes
 */
class ExceptionClassesTest {

    @Test
    void resourceNotFoundException_HasCorrectAnnotation() {
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "ResourceNotFoundException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void resourceNotFoundException_WithResourceNameAndId() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Product", 123L);
        assertEquals("Product nicht gefunden: 123", exception.getMessage());
    }

    @Test
    void resourceNotFoundException_WithResourceNameAndString() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "john@example.com");
        assertEquals("User nicht gefunden: john@example.com", exception.getMessage());
    }

    @Test
    void resourceNotFoundException_WithMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Custom message");
        assertEquals("Custom message", exception.getMessage());
    }

    @Test
    void validationException_HasCorrectAnnotation() {
        ResponseStatus annotation = ValidationException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "ValidationException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
    }

    @Test
    void validationException_WithMessage() {
        ValidationException exception = new ValidationException("Invalid input");
        assertEquals("Invalid input", exception.getMessage());
    }

    @Test
    void validationException_WithCause() {
        Throwable cause = new IllegalArgumentException("Cause");
        ValidationException exception = new ValidationException("Validation failed", cause);
        assertEquals("Validation failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void conflictException_HasCorrectAnnotation() {
        ResponseStatus annotation = ConflictException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "ConflictException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.CONFLICT, annotation.value());
    }

    @Test
    void conflictException_WithMessage() {
        ConflictException exception = new ConflictException("Resource already exists");
        assertEquals("Resource already exists", exception.getMessage());
    }

    @Test
    void unauthorizedException_HasCorrectAnnotation() {
        ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "UnauthorizedException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.FORBIDDEN, annotation.value());
    }

    @Test
    void unauthorizedException_WithMessage() {
        UnauthorizedException exception = new UnauthorizedException("Access denied");
        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void tokenExpiredException_HasCorrectAnnotation() {
        ResponseStatus annotation = TokenExpiredException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "TokenExpiredException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
    }

    @Test
    void invalidBookingStatusException_HasCorrectAnnotation() {
        ResponseStatus annotation = InvalidBookingStatusException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "InvalidBookingStatusException should have @ResponseStatus annotation");
        assertEquals(HttpStatus.CONFLICT, annotation.value());
    }

    @Test
    void fileStorageException_WithMessage() {
        FileStorageException exception = new FileStorageException("Storage failed");
        assertEquals("Storage failed", exception.getMessage());
    }

    @Test
    void fileStorageException_WithCause() {
        Throwable cause = new java.io.IOException("Disk full");
        FileStorageException exception = new FileStorageException("Cannot save file", cause);
        assertEquals("Cannot save file", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
