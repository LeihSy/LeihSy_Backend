package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.repository.ProductRepository;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          LocationRepository locationRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
    }

    // Alle aktiven Products abrufen
    public List<Product> getAllProducts() {
        return productRepository.findAllActive();
    }

    // Product per ID abrufen
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product nicht gefunden: " + id));
    }

    // Products nach Kategorie
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Products nach Location
    public List<Product> getProductsByLocation(Long locationId) {
        return productRepository.findByLocationId(locationId);
    }

    // Products eines Verleihers
    public List<Product> getProductsByLender(Long lenderId) {
        return productRepository.findByLenderId(lenderId);
    }

    // Suche nach Name
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByName(keyword);
    }

    // Volltext-Suche (Name + Description)
    public List<Product> fullTextSearch(String keyword) {
        return productRepository.fullTextSearch(keyword);
    }

    // Neues Product erstellen
    public Product createProduct(Product product, Long categoryId, Long locationId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + categoryId));
            product.setCategory(category);
        }

        if (locationId != null) {
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location nicht gefunden: " + locationId));
            product.setLocation(location);
        }

        return productRepository.save(product);
    }

    // Product aktualisieren
    public Product updateProduct(Long id, Product updatedProduct, Long categoryId, Long locationId) {
        Product product = getProductById(id);

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setExpiryDate(updatedProduct.getExpiryDate());
        product.setPrice(updatedProduct.getPrice());
        product.setImageUrl(updatedProduct.getImageUrl());
        product.setAccessories(updatedProduct.getAccessories());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Kategorie nicht gefunden: " + categoryId));
            product.setCategory(category);
        }

        if (locationId != null) {
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location nicht gefunden: " + locationId));
            product.setLocation(location);
        }

        return productRepository.save(product);
    }

    // Product löschen (Soft-Delete)
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.softDelete();
        productRepository.save(product);
    }
}