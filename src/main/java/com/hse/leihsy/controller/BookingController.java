package com.hse.leihsy.controller;

import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingCreateDTO;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.Booking;
import com.hse.leihsy.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingMapper bookingMapper; // Inject the Mapper

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookingMapper.toDTOs(bookings));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BookingDTO>> getPendingBookings(@RequestParam Long receiverId) {
        List<Booking> bookings = bookingService.getPendingBookingsForReceiver(receiverId);
        return ResponseEntity.ok(bookingMapper.toDTOs(bookings));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BookingDTO>> getOverdueBookings() {
        List<Booking> bookings = bookingService.getOverdueBookings();
        return ResponseEntity.ok(bookingMapper.toDTOs(bookings));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingDTO> confirmBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LocalDateTime confirmedPickup = LocalDateTime.parse(payload.get("confirmedPickup"));
        Booking booking = bookingService.confirmBooking(id, confirmedPickup);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingDTO> rejectBooking(@PathVariable Long id) {
        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/propose")
    public ResponseEntity<BookingDTO> proposeNewPickup(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        Long proposerId = Long.parseLong(payload.get("proposerId"));
        LocalDateTime newProposal = LocalDateTime.parse(payload.get("newProposal"));
        Booking booking = bookingService.proposeNewPickup(id, proposerId, newProposal);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<BookingDTO> recordPickup(@PathVariable Long id) {
        Booking booking = bookingService.recordPickup(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BookingDTO> recordReturn(@PathVariable Long id) {
        Booking booking = bookingService.recordReturn(id);
        return ResponseEntity.ok(bookingMapper.toDTO(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}