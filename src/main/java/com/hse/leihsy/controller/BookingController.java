package com.hse.leihsy.controller;

import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.BookingCreateDTO;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUser(userId);
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BookingDTO>> getPendingBookings(@RequestParam Long receiverId) {
        List<Booking> bookings = bookingService.getPendingBookingsForReceiver(receiverId);
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BookingDTO>> getOverdueBookings() {
        List<Booking> bookings = bookingService.getOverdueBookings();
        List<BookingDTO> dtos = bookings.stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingCreateDTO createDTO) {
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

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LocalDateTime confirmedPickup = LocalDateTime.parse(payload.get("confirmedPickup"));
        Booking booking = bookingService.confirmBooking(id, confirmedPickup);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDTO> rejectBooking(@PathVariable Long id) {
        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @PutMapping("/{id}/propose")
    public ResponseEntity<BookingDTO> proposeNewPickup(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        Long proposerId = Long.parseLong(payload.get("proposerId"));
        LocalDateTime newProposal = LocalDateTime.parse(payload.get("newProposal"));
        Booking booking = bookingService.proposeNewPickup(id, proposerId, newProposal);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<BookingDTO> recordPickup(@PathVariable Long id) {
        Booking booking = bookingService.recordPickup(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BookingDTO> recordReturn(@PathVariable Long id) {
        Booking booking = bookingService.recordReturn(id);
        return ResponseEntity.ok(convertToDTO(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
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