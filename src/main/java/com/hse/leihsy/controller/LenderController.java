package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lenders")
@RequiredArgsConstructor
@Tag(name = "Lender Management", description = "APIs for lender-specific operations")
public class LenderController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @Operation(
            summary = "Get bookings of a lender",
            description = "Returns all bookings where the specified user is the lender. Supports filtering by status and including deleted bookings."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lender not found")
    })
    @GetMapping("/{lenderId}/bookings")
    public ResponseEntity<List<BookingDTO>> getLenderBookings(
            @Parameter(description = "ID of the lender") @PathVariable Long lenderId,

            @Parameter(description = "Filter by status (e.g., 'pending', 'confirmed')")
            @RequestParam(required = false) String status,

            @Parameter(description = "Include soft-deleted bookings (admin only)")
            @RequestParam(required = false, defaultValue = "false") boolean deleted
    ) {
        List<BookingDTO> bookings;

        if ("pending".equalsIgnoreCase(status)) {
            bookings = bookingService.getPendingBookingsByLenderId(lenderId, deleted);
        } else {
            bookings = bookingService.getBookingsByLenderId(lenderId, deleted);
        }

        return ResponseEntity.ok(bookings);
    }
}