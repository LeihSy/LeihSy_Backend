package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingCreateDTO;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/bookings", produces = "application/json")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @Operation(summary = "Get all bookings", description = "Returns a list of all bookings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(List.of());
    }


    @Operation(summary = "Get booking by ID", description = "Returns a booking with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(
            @Parameter(description = "ID of the booking to retrieve") @PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Get bookings by user", description = "Returns a list of bookings for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUser(userId);
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }


    @Operation(summary = "Get pending bookings", description = "Returns a list of pending bookings for a receiver")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending bookings retrieved successfully")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<BookingDTO>> getPendingBookings(
            @Parameter(description = "ID of the receiver") @RequestParam Long receiverId) {
        List<Booking> bookings = bookingService.getPendingBookingsForReceiver(receiverId);
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }


    @Operation(summary = "Get overdue bookings", description = "Returns a list of bookings that are overdue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overdue bookings retrieved successfully")
    })
    @GetMapping("/overdue")
    public ResponseEntity<List<BookingDTO>> getOverdueBookings() {
        List<Booking> bookings = bookingService.getOverdueBookings();
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }


    @Operation(summary = "Create a new booking", description = "Creates a new booking with the provided data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(
            @Parameter(description = "Booking data") @Valid @RequestBody BookingCreateDTO createDTO) {
        Booking booking = bookingService.createBooking(
                1L,
                createDTO.getProductId(),
                createDTO.getStartDate(),
                createDTO.getEndDate(),
                createDTO.getProposalPickup(),
                createDTO.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(booking));
    }


    @Operation(summary = "Confirm a booking", description = "Confirms a booking with the given pickup date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @Parameter(description = "ID of the booking to confirm") @PathVariable Long id,
            @Parameter(description = "Payload containing confirmed pickup datetime") @RequestBody Map<String, String> payload) {

        LocalDateTime confirmedPickup = LocalDateTime.parse(payload.get("confirmedPickup"));
        Booking booking = bookingService.confirmBooking(id, confirmedPickup);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Reject a booking", description = "Rejects a booking by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking rejected successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDTO> rejectBooking(
            @Parameter(description = "ID of the booking to reject") @PathVariable Long id) {
        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Propose a new pickup date", description = "Proposes a new pickup date for a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposal submitted successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/propose")
    public ResponseEntity<BookingDTO> proposeNewPickup(
            @Parameter(description = "ID of the booking to propose a new pickup") @PathVariable Long id,
            @Parameter(description = "Payload containing proposer ID and new proposed pickup date") @RequestBody Map<String, String> payload) {

        Long proposerId = Long.parseLong(payload.get("proposerId"));
        LocalDateTime newProposal = LocalDateTime.parse(payload.get("newProposal"));
        Booking booking = bookingService.proposeNewPickup(id, proposerId, newProposal);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Record pickup", description = "Records the pickup of an item for a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pickup recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/pickup")
    public ResponseEntity<BookingDTO> recordPickup(
            @Parameter(description = "ID of the booking to record pickup") @PathVariable Long id) {
        Booking booking = bookingService.recordPickup(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Record return", description = "Records the return of an item for a booking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Return recorded successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @PutMapping("/{id}/return")
    public ResponseEntity<BookingDTO> recordReturn(
            @Parameter(description = "ID of the booking to record return") @PathVariable Long id) {
        Booking booking = bookingService.recordReturn(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }


    @Operation(summary = "Cancel a booking", description = "Cancels a booking by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "ID of the booking to cancel") @PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setMessage(booking.getMessage());
        dto.setStatus(booking.getStatus());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setProposalPickup(booking.getProposalPickup());
        dto.setConfirmedPickup(booking.getConfirmedPickup());
        dto.setDistributionDate(booking.getDistributionDate());
        dto.setReturnDate(booking.getReturnDate());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserName(booking.getUser().getName());
        }

        if (booking.getReceiver() != null) {
            dto.setReceiverId(booking.getReceiver().getId());
            dto.setReceiverName(booking.getReceiver().getName());
        }

        if (booking.getItem() != null) {
            dto.setItemId(booking.getItem().getId());
            dto.setItemInvNumber(booking.getItem().getInvNumber());

            if (booking.getItem().getProduct() != null) {
                dto.setProductId(booking.getItem().getProduct().getId());
                dto.setProductName(booking.getItem().getProduct().getName());
            }
        }

        if (booking.getProposalBy() != null) {
            dto.setProposalById(booking.getProposalBy().getId());
            dto.setProposalByName(booking.getProposalBy().getName());
        }

        return dto;
    }
}