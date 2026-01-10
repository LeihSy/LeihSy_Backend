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
import org.springframework.security.access.prepost.PreAuthorize;
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
            @ApiResponse(responseCode = "404", description = "Lender not found"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur eigene Bookings oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canViewLenderBookings(#lenderId, authentication)")
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

    @Operation(
            summary = "Get upcoming pickups for lender",
            description = "Returns confirmed bookings that are waiting for pickup. Sorted by pickup date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming pickups retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur eigene Bookings oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canViewLenderBookings(#lenderId, authentication)")
    @GetMapping("/{lenderId}/upcoming")
    public ResponseEntity<List<BookingDTO>> getLenderUpcoming(
            @Parameter(description = "ID of the lender") @PathVariable Long lenderId
    ) {
        List<BookingDTO> bookings = bookingService.getUpcomingBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Get active rentals for lender",
            description = "Returns items currently with the student. Sorted by return date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active rentals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur eigene Bookings oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canViewLenderBookings(#lenderId, authentication)")
    @GetMapping("/{lenderId}/active")
    public ResponseEntity<List<BookingDTO>> getLenderActive(
            @Parameter(description = "ID of the lender") @PathVariable Long lenderId
    ) {
        List<BookingDTO> bookings = bookingService.getActiveBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookings);
    }

    @Operation(
            summary = "Get overdue rentals for lender",
            description = "Returns items that are currently rented but past their return date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue rentals retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Keine Berechtigung - nur eigene Bookings oder Admin")
    })
    @PreAuthorize("hasRole('ADMIN') or @bookingSecurityService.canViewLenderBookings(#lenderId, authentication)")
    @GetMapping("/{lenderId}/overdue")
    public ResponseEntity<List<BookingDTO>> getLenderOverdue(
            @Parameter(description = "ID of the lender") @PathVariable Long lenderId
    ) {
        List<BookingDTO> bookings = bookingService.getOverdueBookingsByLenderId(lenderId);
        return ResponseEntity.ok(bookings);
    }
}
