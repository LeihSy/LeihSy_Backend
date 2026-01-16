package com.hse.leihsy.repository;

import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;
    private Location testLocation;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("VR-Brillen");
        entityManager.persist(testCategory);

        testLocation = new Location();
        testLocation.setRoomNr("Raum A123");
        entityManager.persist(testLocation);

        testProduct = new Product();
        testProduct.setName("Meta Quest 3");
        testProduct.setDescription("VR-Brille mit Mixed Reality");
        testProduct.setCategory(testCategory);
        testProduct.setLocation(testLocation);
        entityManager.persist(testProduct);

        entityManager.flush();
    }

    @Nested
    @DisplayName("findAllActive Tests")
    class FindAllActiveTests {

        @Test
        @DisplayName("Sollte alle aktiven Produkte finden")
        void shouldFindAllActiveProducts() {
            List<Product> result = productRepository.findAllActive();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getName()).isEqualTo("Meta Quest 3");
        }

        @Test
        @DisplayName("Sollte gelöschte Produkte ausschließen")
        void shouldExcludeDeletedProducts() {
            testProduct.setDeletedAt(LocalDateTime.now());
            entityManager.persist(testProduct);
            entityManager.flush();

            List<Product> result = productRepository.findAllActive();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCategoryId Tests")
    class FindByCategoryIdTests {

        @Test
        @DisplayName("Sollte Produkte einer Kategorie finden")
        void shouldFindProductsByCategory() {
            List<Product> result = productRepository.findByCategoryId(testCategory.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getCategory().getName()).isEqualTo("VR-Brillen");
        }

        @Test
        @DisplayName("Sollte leere Liste zurückgeben bei unbekannter Kategorie")
        void shouldReturnEmptyForUnknownCategory() {
            List<Product> result = productRepository.findByCategoryId(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByLocationId Tests")
    class FindByLocationIdTests {

        @Test
        @DisplayName("Sollte Produkte eines Standorts finden")
        void shouldFindProductsByLocation() {
            List<Product> result = productRepository.findByLocationId(testLocation.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getLocation().getRoomNr()).isEqualTo("Raum A123");
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {

        @Test
        @DisplayName("Sollte Produkte nach Namen suchen")
        void shouldSearchProductsByName() {
            List<Product> result = productRepository.searchByName("Meta");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Sollte case-insensitive suchen")
        void shouldSearchCaseInsensitive() {
            List<Product> result = productRepository.searchByName("meta");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Sollte leere Liste bei keinem Treffer zurückgeben")
        void shouldReturnEmptyWhenNoMatch() {
            List<Product> result = productRepository.searchByName("iPhone");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("fullTextSearch Tests")
    class FullTextSearchTests {

        @Test
        @DisplayName("Sollte in Name und Description suchen")
        void shouldSearchInNameAndDescription() {
            List<Product> result = productRepository.fullTextSearch("Mixed Reality");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Sollte case-insensitive in Description suchen")
        void shouldSearchDescriptionCaseInsensitive() {
            List<Product> result = productRepository.fullTextSearch("vr-brille");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countByCategoryId Tests")
    class CountByCategoryIdTests {

        @Test
        @DisplayName("Sollte Produkte einer Kategorie zählen")
        void shouldCountProductsByCategory() {
            long count = productRepository.countByCategoryId(testCategory.getId());

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("Sollte 0 zurückgeben wenn keine Produkte")
        void shouldReturnZeroWhenNoProducts() {
            Category emptyCategory = new Category();
            emptyCategory.setName("Empty Category");
            entityManager.persist(emptyCategory);
            entityManager.flush();

            long count = productRepository.countByCategoryId(emptyCategory.getId());

            assertThat(count).isZero();
        }
    }
}
