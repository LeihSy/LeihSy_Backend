package com.hse.leihsy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.exception.ConflictException;
import com.hse.leihsy.exception.ValidationException;
import com.hse.leihsy.mapper.BookingMapper;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private Booking testBooking;

    @BeforeEach
    void setUp() {
        User testUser = new User("keycloak-123", "Test User");
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        User testLender = new User("keycloak-456", "Test Lender");
        testLender.setId(2L);
        testLender.setEmail("lender@example.com");

        Product testProduct = new Product("Test Product", "Description");
        testProduct.setId(1L);

        Item testItem = new Item();
        testItem.setId(1L);
        testItem.setProduct(testProduct);
        testItem.setLender(testLender);
        testItem.setInvNumber("INV-001");

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setLender(testLender);
        testBooking.setItem(testItem);
        testBooking.setStartDate(LocalDateTime.now().plusDays(1));
        testBooking.setEndDate(LocalDateTime.now().plusDays(7));
        testBooking.setStatus(BookingStatus.PENDING.name());
    }

    @Nested
    @DisplayName("getAllBookings Tests")
    class GetAllBookingsTests {

        @Test
        @DisplayName("Sollte alle Bookings zurückgeben")
        void shouldReturnAllBookings() {
            when(bookingRepository.findAll()).thenReturn(List.of(testBooking));
            when(bookingMapper.toDTOList(any())).thenReturn(List.of(new BookingDTO()));

            List<BookingDTO> result = bookingService.getAllBookings();

            assertThat(result).hasSize(1);
            verify(bookingRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getBookingsByUserId Tests")
    class GetBookingsByUserIdTests {

        @Test
        @DisplayName("Sollte Bookings eines Users zurückgeben")
        void shouldReturnBookingsForUser() {
            when(bookingRepository.findByUserId(1L)).thenReturn(List.of(testBooking));
            when(bookingMapper.toDTOList(any())).thenReturn(List.of(new BookingDTO()));

            List<BookingDTO> result = bookingService.getBookingsByUserId(1L);

            assertThat(result).hasSize(1);
            verify(bookingRepository).findByUserId(1L);
        }
    }

    @Nested
    @DisplayName("confirmBooking Tests")
    class ConfirmBookingTests {

        @Test
        @DisplayName("Sollte Booking bestätigen mit Abholtermin")
        void shouldConfirmBookingWithPickupTime() throws Exception {
            LocalDateTime pickupTime = LocalDateTime.now().plusDays(1);
            List<LocalDateTime> pickups = List.of(pickupTime);

            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(objectMapper.writeValueAsString(any())).thenReturn("[\"2025-01-01T10:00:00\"]");
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toDTO(any(Booking.class))).thenReturn(new BookingDTO());

            BookingDTO result = bookingService.confirmBooking(1L, pickups);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn bereits bestätigt")
        void shouldThrowWhenAlreadyConfirmed() {
            testBooking.setConfirmedPickup(LocalDateTime.now());
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.confirmBooking(1L, List.of(LocalDateTime.now())))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already confirmed");
        }
    }

    @Nested
    @DisplayName("recordPickup Tests")
    class RecordPickupTests {

        @Test
        @DisplayName("Sollte Abholung dokumentieren")
        void shouldRecordPickup() {
            testBooking.setConfirmedPickup(LocalDateTime.now());
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toDTO(any(Booking.class))).thenReturn(new BookingDTO());

            BookingDTO result = bookingService.recordPickup(1L);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn noch nicht bestätigt")
        void shouldThrowWhenNotConfirmed() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.recordPickup(1L))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn bereits abgeholt")
        void shouldThrowWhenAlreadyPickedUp() {
            testBooking.setConfirmedPickup(LocalDateTime.now());
            testBooking.setDistributionDate(LocalDateTime.now());
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.recordPickup(1L))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("recordReturn Tests")
    class RecordReturnTests {

        @Test
        @DisplayName("Sollte Rückgabe dokumentieren")
        void shouldRecordReturn() {
            testBooking.setConfirmedPickup(LocalDateTime.now());
            testBooking.setDistributionDate(LocalDateTime.now());
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toDTO(any(Booking.class))).thenReturn(new BookingDTO());

            BookingDTO result = bookingService.recordReturn(1L);

            assertThat(result).isNotNull();
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn noch nicht abgeholt")
        void shouldThrowWhenNotPickedUp() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.recordReturn(1L))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("cancelBooking Tests")
    class CancelBookingTests {

        @Test
        @DisplayName("Sollte Booking stornieren")
        void shouldCancelBooking() {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

            bookingService.cancelBooking(1L);

            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen wenn bereits abgeholt")
        void shouldThrowWhenAlreadyPickedUp() {
            testBooking.setDistributionDate(LocalDateTime.now());
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

            assertThatThrownBy(() -> bookingService.cancelBooking(1L))
                    .isInstanceOf(ConflictException.class);
        }
    }
}
