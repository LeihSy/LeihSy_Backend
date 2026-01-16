package com.hse.leihsy.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Booking Entity Logic Tests")
class BookingEntityTest {

    @Nested
    @DisplayName("calculateStatus() Tests")
    class CalculateStatusTests {

        @Test
        @DisplayName("Sollte PENDING zurückgeben wenn frisch erstellt")
        void shouldReturnPendingWhenFresh() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now()); // Gerade eben erstellt

            // Keine weiteren Daten gesetzt

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.PENDING);
        }

        @Test
        @DisplayName("Sollte CANCELLED zurückgeben wenn älter als 24h und nicht bestätigt")
        void shouldReturnCancelledWhenOver24h() {
            Booking booking = new Booking();
            // Erstellt vor 25 Stunden
            booking.setCreatedAt(LocalDateTime.now().minusHours(25));

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.CANCELLED);
        }

        @Test
        @DisplayName("Sollte CONFIRMED zurückgeben wenn bestätigt und Abholzeit in Zukunft")
        void shouldReturnConfirmedWhenPickupFuture() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now().minusHours(2));
            // Abholung in 1 Stunde
            booking.setConfirmedPickup(LocalDateTime.now().plusHours(1));

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Sollte CONFIRMED zurückgeben wenn bestätigt und Abholzeit vor kurzem (< 24h)")
        void shouldReturnConfirmedWhenPickupRecent() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now().minusDays(2));
            // Abholung war vor 23 Stunden (noch nicht abgelaufen)
            booking.setConfirmedPickup(LocalDateTime.now().minusHours(23));

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Sollte EXPIRED zurückgeben wenn bestätigt und Abholzeit > 24h her")
        void shouldReturnExpiredWhenPickupOver24hAgo() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now().minusDays(5));
            // Abholung war vor 25 Stunden (Frist abgelaufen)
            booking.setConfirmedPickup(LocalDateTime.now().minusHours(25));

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.EXPIRED);
        }

        @Test
        @DisplayName("Sollte PICKED_UP zurückgeben wenn distributionDate gesetzt (Priorität über Expired)")
        void shouldReturnPickedUpWhenDistributed() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now().minusDays(10));
            // Pickup war vor langer Zeit (wäre eigentlich expired)
            booking.setConfirmedPickup(LocalDateTime.now().minusDays(5));
            // Aber: Wurde tatsächlich abgeholt
            booking.setDistributionDate(LocalDateTime.now().minusDays(5));

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.PICKED_UP);
        }

        @Test
        @DisplayName("Sollte RETURNED zurückgeben wenn returnDate gesetzt (Höchste Prio)")
        void shouldReturnReturnedWhenReturned() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now().minusDays(10));
            booking.setConfirmedPickup(LocalDateTime.now().minusDays(8));
            booking.setDistributionDate(LocalDateTime.now().minusDays(8));
            // Wurde zurückgegeben
            booking.setReturnDate(LocalDateTime.now());

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.RETURNED);
        }

        @Test
        @DisplayName("Sollte REJECTED zurückgeben wenn deletedAt gesetzt (Soft Delete)")
        void shouldReturnRejectedWhenDeleted() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now());
            // Wurde gelöscht/abgelehnt
            booking.setDeletedAt(LocalDateTime.now());

            // Selbst wenn andere Daten gesetzt wären
            booking.setConfirmedPickup(LocalDateTime.now());

            assertThat(booking.calculateStatus()).isEqualTo(BookingStatus.REJECTED);
        }
    }

    @Nested
    @DisplayName("Constructor & Helper Tests")
    class ConstructorAndHelperTests {

        @Test
        @DisplayName("Konstruktor sollte Lender automatisch aus Item setzen")
        void shouldSetLenderFromItemInConstructor() {
            // Arrange
            User lender = new User();
            lender.setId(99L);
            lender.setName("Lender User");

            Item item = new Item();
            item.setId(1L);
            item.setLender(lender); // Item gehört diesem Lender

            User borrower = new User();
            borrower.setId(2L);

            // Act
            Booking booking = new Booking(borrower, item, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

            // Assert
            assertThat(booking.getLender()).isNotNull();
            assertThat(booking.getLender().getId()).isEqualTo(99L);
            assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING.name());
        }

        @Test
        @DisplayName("isGroupBooking sollte true zurückgeben wenn Gruppe gesetzt")
        void shouldIdentifyGroupBooking() {
            Booking booking = new Booking();
            booking.setStudentGroup(new StudentGroup());

            assertThat(booking.isGroupBooking()).isTrue();
        }

        @Test
        @DisplayName("isGroupBooking sollte false zurückgeben wenn keine Gruppe")
        void shouldIdentifySingleBooking() {
            Booking booking = new Booking();
            booking.setStudentGroup(null);

            assertThat(booking.isGroupBooking()).isFalse();
        }

        @Test
        @DisplayName("updateStatus sollte das String-Feld aktualisieren")
        void shouldUpdateStatusString() {
            Booking booking = new Booking();
            booking.setCreatedAt(LocalDateTime.now()); // -> PENDING logic

            // Initial ist status null oder alt
            booking.setStatus(null);

            // Act
            booking.updateStatus();

            // Assert
            assertThat(booking.getStatus()).isEqualTo("PENDING");

            // Change state -> CANCELLED logic
            booking.setCreatedAt(LocalDateTime.now().minusHours(26));

            // Act
            booking.updateStatus();

            // Assert
            assertThat(booking.getStatus()).isEqualTo("CANCELLED");
        }
    }
}