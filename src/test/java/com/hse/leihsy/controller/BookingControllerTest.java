package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.config.TestSecurityConfig;
import com.hse.leihsy.config.UserSyncFilter;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.User;
import com.hse.leihsy.service.BookingService;
import com.hse.leihsy.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("BookingController Functional Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS ---

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserSyncFilter userSyncFilter;

    private User mockUser;

    @BeforeEach
    void setUp() throws Exception {
        // Filter Chain Bypass
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userSyncFilter).doFilter(any(), any(), any());

        // Mock User setup
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUniqueId("test-user");
    }

    @Test
    @DisplayName("POST /api/bookings - Sollte 201 Created liefern bei valider Buchung")
    void createBooking_Valid_ShouldReturn201() throws Exception {
        // Arrange
        BookingController.CreateBookingRequest request = new BookingController.CreateBookingRequest();
        request.setProductId(10L);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(5));
        request.setMessage("Projektarbeit");
        request.setQuantity(1);

        BookingDTO responseDTO = new BookingDTO();
        responseDTO.setId(100L);
        responseDTO.setStatus("PENDING");

        // Controller ruft userService.getCurrentUser() auf
        when(userService.getCurrentUser()).thenReturn(mockUser);

        // Service erwartet Einzelparameter, kein DTO
        when(bookingService.createBooking(
                eq(1L), // userId
                eq(10L), // productId
                any(LocalDateTime.class), // start
                any(LocalDateTime.class), // end
                eq("Projektarbeit"), // message
                eq(1), // quantity
                isNull() // groupId (optional)
        )).thenReturn(List.of(responseDTO)); // Controller gibt Liste zurück

        // Act & Assert
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/bookings - Sollte 200 OK und Liste liefern (Admin)")
    void getAllBookings_ShouldReturnList() throws Exception {
        // Arrange
        when(bookingService.getAllBookings(null)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Sollte 200 OK liefern")
    void getBookingById_ShouldReturnBooking() throws Exception {
        // Arrange
        BookingDTO dto = new BookingDTO();
        dto.setId(123L);
        when(bookingService.getBookingDTOById(123L)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Sollte 404 liefern wenn nicht gefunden")
    void getBooking_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(bookingService.getBookingDTOById(999L))
                .thenThrow(new ResourceNotFoundException("Booking", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/bookings/999"))
                .andExpect(status().isNotFound());
    }
}