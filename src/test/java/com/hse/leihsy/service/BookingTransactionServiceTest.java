package com.hse.leihsy.service;

import com.hse.leihsy.exception.*;
import com.hse.leihsy.model.dto.BookingDTO;
import com.hse.leihsy.model.dto.TransactionDTO;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.BookingRepository;
import com.hse.leihsy.repository.BookingTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingTransactionService Tests")
class BookingTransactionServiceTest {

    @Mock
    private BookingTransactionRepository transactionRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingTransactionService transactionService;

    private User borrower;
    private User lender;
    private User stranger;
    private Booking bookingMock;
    private BookingTransaction transaction;

    @BeforeEach
    void setUp() {
        // Inject @Value field manually for Unit Test
        ReflectionTestUtils.setField(transactionService, "tokenExpiryMinutes", 15);

        // Setup Users
        borrower = new User("user-1", "Student");
        borrower.setId(1L);

        lender = new User("lender-1", "Lender");
        lender.setId(2L);

        stranger = new User("stranger", "Stranger");
        stranger.setId(99L);

        // Mock Booking Entity to control calculateStatus()
        bookingMock = mock(Booking.class);
        lenient().when(bookingMock.getId()).thenReturn(100L);
        lenient().when(bookingMock.getUser()).thenReturn(borrower);
        lenient().when(bookingMock.getLender()).thenReturn(lender);
        lenient().when(bookingMock.getStudentGroup()).thenReturn(null);

        // Setup Transaction
        transaction = BookingTransaction.builder()
                // .id(50L) entfernt, da BaseEntity-Felder nicht im Standard-Builder sind
                .token("ABC12345")
                .booking(bookingMock)
                .createdBy(borrower)
                .transactionType(TransactionType.PICKUP)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .usedAt(null)
                .build();

        // ID manuell via Setter setzen (BaseEntity hat Setter)
        transaction.setId(50L);
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Sollte PICKUP Token generieren wenn Booking CONFIRMED")
        void shouldGeneratePickupToken() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(borrower);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(bookingMock));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.CONFIRMED);

            // Kein existierender Token -> generiere neu
            when(transactionRepository.findValidToken(100L, TransactionType.PICKUP))
                    .thenReturn(Optional.empty());
            when(transactionRepository.save(any(BookingTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TransactionDTO result = transactionService.generateToken(100L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getToken()).hasSize(8);
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.PICKUP);

            verify(transactionRepository).invalidatePreviousTokens(100L, TransactionType.PICKUP);
            verify(transactionRepository).save(any(BookingTransaction.class));
        }

        @Test
        @DisplayName("Sollte RETURN Token generieren wenn Booking PICKED_UP")
        void shouldGenerateReturnToken() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(borrower);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(bookingMock));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.PICKED_UP);

            when(transactionRepository.findValidToken(100L, TransactionType.RETURN))
                    .thenReturn(Optional.empty());
            when(transactionRepository.save(any(BookingTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            TransactionDTO result = transactionService.generateToken(100L);

            // Assert
            assertThat(result.getTransactionType()).isEqualTo(TransactionType.RETURN);
        }

        @Test
        @DisplayName("Sollte existierenden gültigen Token zurückgeben (Idempotenz)")
        void shouldReturnExistingTokenIfValid() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(borrower);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(bookingMock));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.CONFIRMED);

            // Existierender Token gefunden
            when(transactionRepository.findValidToken(100L, TransactionType.PICKUP))
                    .thenReturn(Optional.of(transaction));

            // Act
            TransactionDTO result = transactionService.generateToken(100L);

            // Assert
            assertThat(result.getToken()).isEqualTo("ABC12345");
            // Verify SAVE was NOT called
            verify(transactionRepository, never()).save(any(BookingTransaction.class));
            // Verify invalidate was NOT called
            verify(transactionRepository, never()).invalidatePreviousTokens(anyLong(), any());
        }

        @Test
        @DisplayName("Sollte UnauthorizedException werfen wenn User nicht berechtigt")
        void shouldThrowWhenUserNotAuthorized() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(stranger); // Stranger attempts to generate
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(bookingMock));

            // Act & Assert
            assertThatThrownBy(() -> transactionService.generateToken(100L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Nur der Entleiher");
        }

        @Test
        @DisplayName("Sollte InvalidBookingStatusException werfen wenn Status falsch")
        void shouldThrowWhenStatusInvalid() {
            // Arrange
            when(userService.getCurrentUser()).thenReturn(borrower);
            when(bookingRepository.findById(100L)).thenReturn(Optional.of(bookingMock));
            // PENDING ist kein gültiger Status für Token Generierung
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.PENDING);

            // Act & Assert
            assertThatThrownBy(() -> transactionService.generateToken(100L))
                    .isInstanceOf(InvalidBookingStatusException.class);
        }
    }

    @Nested
    @DisplayName("executeTransaction Tests")
    class ExecuteTransactionTests {

        @Test
        @DisplayName("Sollte PICKUP erfolgreich ausführen")
        void shouldExecutePickupTransaction() {
            // Arrange
            transaction.setTransactionType(TransactionType.PICKUP);

            when(userService.getCurrentUser()).thenReturn(lender); // Lender scannt
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.CONFIRMED); // Status muss passen

            when(bookingService.recordPickup(100L)).thenReturn(new BookingDTO());

            // Act
            transactionService.executeTransaction("ABC12345");

            // Assert
            assertThat(transaction.getUsedAt()).isNotNull(); // Token muss als benutzt markiert sein
            verify(bookingService).recordPickup(100L);
            verify(transactionRepository).save(transaction);
        }

        @Test
        @DisplayName("Sollte RETURN erfolgreich ausführen")
        void shouldExecuteReturnTransaction() {
            // Arrange
            transaction.setTransactionType(TransactionType.RETURN);

            when(userService.getCurrentUser()).thenReturn(lender);
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.PICKED_UP); // Status muss passen

            when(bookingService.recordReturn(100L)).thenReturn(new BookingDTO());

            // Act
            transactionService.executeTransaction("ABC12345");

            // Assert
            verify(bookingService).recordReturn(100L);
        }

        @Test
        @DisplayName("Sollte Exception werfen wenn Token nicht gefunden")
        void shouldThrowWhenTokenNotFound() {
            when(userService.getCurrentUser()).thenReturn(lender);
            when(transactionRepository.findByToken("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.executeTransaction("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Sollte TokenExpiredException werfen wenn Token abgelaufen")
        void shouldThrowWhenTokenExpired() {
            transaction.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Abgelaufen
            when(userService.getCurrentUser()).thenReturn(lender);
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> transactionService.executeTransaction("ABC12345"))
                    .isInstanceOf(TokenExpiredException.class);
        }

        @Test
        @DisplayName("Sollte ConflictException werfen wenn Token bereits benutzt")
        void shouldThrowWhenTokenAlreadyUsed() {
            transaction.setUsedAt(LocalDateTime.now());
            when(userService.getCurrentUser()).thenReturn(lender);
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> transactionService.executeTransaction("ABC12345"))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Sollte UnauthorizedException werfen wenn nicht Lender scannt")
        void shouldThrowWhenScannerNotLender() {
            when(userService.getCurrentUser()).thenReturn(stranger);
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> transactionService.executeTransaction("ABC12345"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("Sollte InvalidBookingStatusException werfen wenn Booking Status nicht passt")
        void shouldThrowWhenBookingStatusMismatch() {
            // Szenario: Token ist für PICKUP, aber Item ist schon PICKED_UP
            transaction.setTransactionType(TransactionType.PICKUP);

            when(userService.getCurrentUser()).thenReturn(lender);
            when(transactionRepository.findByToken("ABC12345")).thenReturn(Optional.of(transaction));
            when(bookingMock.calculateStatus()).thenReturn(BookingStatus.PICKED_UP);

            assertThatThrownBy(() -> transactionService.executeTransaction("ABC12345"))
                    .isInstanceOf(InvalidBookingStatusException.class);
        }
    }
}