package com.hse.leihsy.exception;

import com.hse.leihsy.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestExceptionController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testResourceNotFoundException_Returns404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void testValidationException_Returns400() throws Exception {
        mockMvc.perform(get("/test/validation-error"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testConflictException_Returns409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Resource already exists"));
    }

    @Test
    void testUnauthorizedException_Returns403() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void testTokenExpiredException_Returns400() throws Exception {
        mockMvc.perform(get("/test/token-expired"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Token has expired"));
    }

    @Test
    void testInvalidBookingStatusException_Returns409() throws Exception {
        mockMvc.perform(get("/test/invalid-status"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Invalid booking status"));
    }

    @Test
    void testFileStorageException_Returns500() throws Exception {
        mockMvc.perform(get("/test/file-storage-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Fehler beim Speichern der Datei"));
    }

    @Test
    void testGenericRuntimeException_Returns500() throws Exception {
        mockMvc.perform(get("/test/runtime-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ein unerwarteter Fehler ist aufgetreten"));
    }

    /**
     * Test Controller that throws various exceptions for testing
     * to ensure proper Spring component scanning in tests
     */
    @RestController
    @RequestMapping("/test")
    public static class TestExceptionController {

        @GetMapping("/not-found")
        public void throwResourceNotFoundException() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/validation-error")
        public void throwValidationException() {
            throw new ValidationException("Validation failed");
        }

        @GetMapping("/conflict")
        public void throwConflictException() {
            throw new ConflictException("Resource already exists");
        }

        @GetMapping("/unauthorized")
        public void throwUnauthorizedException() {
            throw new UnauthorizedException("Access denied");
        }

        @GetMapping("/token-expired")
        public void throwTokenExpiredException() {
            throw new TokenExpiredException("Token has expired");
        }

        @GetMapping("/invalid-status")
        public void throwInvalidBookingStatusException() {
            throw new InvalidBookingStatusException("Invalid booking status");
        }

        @GetMapping("/file-storage-error")
        public void throwFileStorageException() {
            throw new FileStorageException("File storage failed");
        }

        @GetMapping("/runtime-error")
        public void throwRuntimeException() {
            throw new RuntimeException("Unexpected error");
        }
    }
}