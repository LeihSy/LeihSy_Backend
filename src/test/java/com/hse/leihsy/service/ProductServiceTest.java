package com.hse.leihsy.service;

import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.LocationRepository;
import com.hse.leihsy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setRoomNr("Test Location");

        testProduct = new Product("Test Product", "Test Description");
        testProduct.setId(1L);
        testProduct.setCategory(testCategory);
        testProduct.setLocation(testLocation);
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Sollte alle aktiven Produkte zurückgeben")
        void shouldReturnAllActiveProducts() {
            when(productRepository.findAllActive()).thenReturn(List.of(testProduct));

            List<Product> result = productService.getAllProducts();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getName()).isEqualTo("Test Product");
            verify(productRepository).findAllActive();
        }

        @Test
        @DisplayName("Sollte leere Liste zurückgeben wenn keine Produkte")
        void shouldReturnEmptyListWhenNoProducts() {
            when(productRepository.findAllActive()).thenReturn(List.of());

            List<Product> result = productService.getAllProducts();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Sollte Produkt anhand ID finden")
        void shouldFindProductById() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            Product result = productService.getProductById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Sollte ResourceNotFoundException werfen wenn nicht gefunden")
        void shouldThrowWhenNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProductsByCategory Tests")
    class GetProductsByCategoryTests {

        @Test
        @DisplayName("Sollte Produkte einer Kategorie zurückgeben")
        void shouldReturnProductsByCategory() {
            when(productRepository.findByCategoryId(1L)).thenReturn(List.of(testProduct));

            List<Product> result = productService.getProductsByCategory(1L);

            assertThat(result).hasSize(1);
            verify(productRepository).findByCategoryId(1L);
        }
    }

    @Nested
    @DisplayName("searchProducts Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Sollte Produkte nach Suchbegriff finden")
        void shouldSearchProducts() {
            when(productRepository.searchByName("Test")).thenReturn(List.of(testProduct));

            List<Product> result = productService.searchProducts("Test");

            assertThat(result).hasSize(1);
            verify(productRepository).searchByName("Test");
        }
    }

    @Nested
    @DisplayName("fullTextSearch Tests")
    class FullTextSearchTests {

        @Test
        @DisplayName("Sollte Volltext-Suche durchführen")
        void shouldPerformFullTextSearch() {
            when(productRepository.fullTextSearch("Description")).thenReturn(List.of(testProduct));

            List<Product> result = productService.fullTextSearch("Description");

            assertThat(result).hasSize(1);
            verify(productRepository).fullTextSearch("Description");
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Sollte neues Produkt erstellen")
        void shouldCreateProduct() {
            Product newProduct = new Product("New Product", "New Description");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);

            Product result = productService.createProduct(newProduct, 1L, 1L, null);

            assertThat(result).isNotNull();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Sollte Fehler werfen bei unbekannter Kategorie")
        void shouldThrowWhenCategoryNotFound() {
            Product newProduct = new Product("New Product", "New Description");
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(newProduct, 999L, null, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Sollte Produkt soft-deleten")
        void shouldSoftDeleteProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            productService.deleteProduct(1L);

            verify(productRepository).save(any(Product.class));
        }
    }
}
