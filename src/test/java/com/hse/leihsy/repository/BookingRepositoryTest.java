package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BookingRepository Tests")
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private User testLender;
    private Item testItem;

    @BeforeEach
    void setUp() {
        Category testCategory = new Category();
        testCategory.setName("Test Category");
        entityManager.persist(testCategory);

        Location testLocation = new Location();
        testLocation.setRoomNr("Test Location");
        entityManager.persist(testLocation);

        testUser = new User();
        testUser.setUniqueId("user-123");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setBudget(BigDecimal.ZERO);
        entityManager.persist(testUser);

        testLender = new User();
        testLender.setUniqueId("lender-456");
        testLender.setName("Test Lender");
        testLender.setEmail("lender@example.com");
        testLender.setBudget(BigDecimal.ZERO);
        entityManager.persist(testLender);

        Product testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setCategory(testCategory);
        testProduct.setLocation(testLocation);
        entityManager.persist(testProduct);

        testItem = new Item();
        testItem.setProduct(testProduct);
        testItem.setLender(testLender);
        testItem.setInvNumber("INV-001");
        entityManager.persist(testItem);

        Booking testBooking = new Booking();
        testBooking.setUser(testUser);
        testBooking.setLender(testLender);
        testBooking.setItem(testItem);
        testBooking.setStartDate(LocalDateTime.now().plusDays(1));
        testBooking.setEndDate(LocalDateTime.now().plusDays(7));
        testBooking.setStatus(BookingStatus.PENDING.name());
        entityManager.persist(testBooking);

        entityManager.flush();
    }

    private Booking createBooking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setLender(testLender);
        booking.setItem(testItem);
        booking.setStartDate(LocalDateTime.now().plusDays(1));
        booking.setEndDate(LocalDateTime.now().plusDays(7));
        booking.setStatus(status.name());

        if (status == BookingStatus.CONFIRMED || status == BookingStatus.PICKED_UP || status == BookingStatus.RETURNED) {
            booking.setConfirmedPickup(LocalDateTime.now());
        }
        if (status == BookingStatus.PICKED_UP || status == BookingStatus.RETURNED) {
            booking.setDistributionDate(LocalDateTime.now());
        }
        if (status == BookingStatus.RETURNED) {
            booking.setReturnDate(LocalDateTime.now());
        }

        return entityManager.persist(booking);
    }

    @Nested
    @DisplayName("findByUserId Tests")
    class FindByUserIdTests {

        @Test
        @DisplayName("Sollte Bookings eines Users finden")
        void shouldFindBookingsByUserId() {
            entityManager.flush();

            List<Booking> result = bookingRepository.findByUserId(testUser.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Sollte gelöschte Bookings ausschließen")
        void shouldExcludeDeletedBookings() {
            Booking booking = createBooking(BookingStatus.PENDING);
            booking.setDeletedAt(LocalDateTime.now());
            entityManager.persist(booking);
            entityManager.flush();

            List<Booking> result = bookingRepository.findByUserId(testUser.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("findByLenderId Tests")
    class FindByLenderIdTests {

        @Test
        @DisplayName("Sollte Bookings eines Verleihers finden")
        void shouldFindBookingsByLenderId() {
            entityManager.flush();

            List<Booking> result = bookingRepository.findByLenderId(testLender.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getLender().getId()).isEqualTo(testLender.getId());
        }
    }

    @Nested
    @DisplayName("findOverlappingBookings Tests")
    class FindOverlappingBookingsTests {

        @Test
        @DisplayName("Sollte überlappende Bookings finden")
        void shouldFindOverlappingBookings() {
            Booking booking = createBooking(BookingStatus.CONFIRMED);
            booking.setStartDate(LocalDateTime.now().plusDays(10));
            booking.setEndDate(LocalDateTime.now().plusDays(15));
            entityManager.persist(booking);
            entityManager.flush();

            List<Booking> result = bookingRepository.findOverlappingBookings(
                    testItem.getId(),
                    LocalDateTime.now().plusDays(12),
                    LocalDateTime.now().plusDays(20)
            );

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
        }

        @Test
        @DisplayName("Sollte keine überlappenden Bookings finden wenn keine Überlappung")
        void shouldNotFindWhenNoOverlap() {
            Booking booking = createBooking(BookingStatus.CONFIRMED);
            booking.setStartDate(LocalDateTime.now().plusDays(1));
            booking.setEndDate(LocalDateTime.now().plusDays(3));
            entityManager.persist(booking);
            entityManager.flush();

            List<Booking> result = bookingRepository.findOverlappingBookings(
                    testItem.getId(),
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(25)
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOverdue Tests")
    class FindOverdueTests {

        @Test
        @DisplayName("Sollte überfällige Bookings finden")
        void shouldFindOverdueBookings() {
            Booking booking = createBooking(BookingStatus.PICKED_UP);
            booking.setEndDate(LocalDateTime.now().minusDays(1));
            entityManager.persist(booking);
            entityManager.flush();

            List<Booking> result = bookingRepository.findOverdue(LocalDateTime.now());

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllActive Tests")
    class FindAllActiveTests {

        @Test
        @DisplayName("Sollte alle aktiven Bookings finden")
        void shouldFindAllActiveBookings() {
            createBooking(BookingStatus.PENDING);
            createBooking(BookingStatus.CONFIRMED);
            entityManager.flush();

            List<Booking> result = bookingRepository.findAllActive();
            assertThat(result).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findAllPickedUp Tests")
    class FindAllPickedUpTests {

        @Test
        @DisplayName("Sollte alle abgeholten Bookings finden")
        void shouldFindAllPickedUpBookings() {
            createBooking(BookingStatus.PICKED_UP);
            entityManager.flush();

            List<Booking> result = bookingRepository.findAllPickedUp();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllReturned Tests")
    class FindAllReturnedTests {

        @Test
        @DisplayName("Sollte alle zurückgegebenen Bookings finden")
        void shouldFindAllReturnedBookings() {
            createBooking(BookingStatus.RETURNED);
            entityManager.flush();

            List<Booking> result = bookingRepository.findAllReturned();

            assertThat(result).hasSize(1);
        }
    }
}