package com.hse.leihsy.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler without Spring Context
 */
class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFoundException_Returns404() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Product", 123L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleResourceNotFound(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Product nicht gefunden: 123", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleValidationException_Returns400() {
        ValidationException exception = new ValidationException("Invalid input data");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleValidation(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Invalid input data", response.getBody().message());
    }

    @Test
    void handleConflictException_Returns409() {
        ConflictException exception = new ConflictException("Resource already exists");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleConflict(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Resource already exists", response.getBody().message());
    }

    @Test
    void handleUnauthorizedException_Returns403() {
        UnauthorizedException exception = new UnauthorizedException("Access denied");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleUnauthorized(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().status());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void handleTokenExpiredException_Returns400() {
        TokenExpiredException exception = new TokenExpiredException("Token has expired");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleTokenExpired(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("Token has expired", response.getBody().message());
    }

    @Test
    void handleInvalidBookingStatusException_Returns409() {
        InvalidBookingStatusException exception =
                new InvalidBookingStatusException("Cannot perform action in current status");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleInvalidStatus(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().status());
        assertEquals("Cannot perform action in current status", response.getBody().message());
    }

    @Test
    void handleFileStorageException_Returns500() {
        FileStorageException exception = new FileStorageException("Failed to save file");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleFileStorage(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Fehler beim Speichern der Datei", response.getBody().message());
    }

    @Test
    void handleRuntimeException_Returns500() {
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleRuntimeException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("Ein unerwarteter Fehler ist aufgetreten", response.getBody().message());
    }

    @Test
    void handleMethodArgumentNotValidException_Returns400WithFieldErrors() {
        // Mock MethodArgumentNotValidException
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "email", "Invalid email format");
        FieldError fieldError2 = new FieldError("object", "name", "Name is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<Map<String, String>> response =
                handler.handleValidationErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Invalid email format", response.getBody().get("email"));
        assertEquals("Name is required", response.getBody().get("name"));
    }

    @Test
    void errorResponse_ConstructorWorks() {
        GlobalExceptionHandler.ErrorResponse errorResponse =
                new GlobalExceptionHandler.ErrorResponse(404, "Not found", java.time.LocalDateTime.now());

        assertEquals(404, errorResponse.status());
        assertEquals("Not found", errorResponse.message());
        assertNotNull(errorResponse.timestamp());
    }
}
