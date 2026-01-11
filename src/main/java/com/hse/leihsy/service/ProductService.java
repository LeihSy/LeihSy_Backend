package com.hse.leihsy.service;

import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.model.entity.Category;
import com.hse.leihsy.model.entity.Location;
import com.hse.leihsy.repository.ProductRepository;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ImageService imageService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          LocationRepository locationRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.imageService = imageService;
    }

    // Alle aktiven Products abrufen
    public List<Product> getAllProducts() {
        return productRepository.findAllActive();
    }

    // Product per ID abrufen
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // Products nach Kategorie
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    // Products nach Location
    public List<Product> getProductsByLocation(Long locationId) {
        return productRepository.findByLocationId(locationId);
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
    public Product createProduct(Product product, Long categoryId, Long locationId, MultipartFile image) {
        // Image Upload handling
        if (image != null && !image.isEmpty()) {
            String filename = imageService.saveImage(image, product.getName());
            product.setImageUrl("/api/images/" + filename);
        }

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kategorie", categoryId));
            product.setCategory(category);
        }

        if (locationId != null) {
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
            product.setLocation(location);
        }

        return productRepository.save(product);
    }

    // Product aktualisieren
    public Product updateProduct(Long id, Product updatedProduct, Long categoryId, Long locationId, MultipartFile image) {
        Product product = getProductById(id);

        // Image Upload handling - altes Bild löschen falls neues kommt
        if (image != null && !image.isEmpty()) {
            if (product.getImageUrl() != null && product.getImageUrl().startsWith("/api/images/")) {
                String oldFilename = product.getImageUrl().replace("/api/images/", "");
                try {
                    imageService.deleteImage(oldFilename);
                } catch (Exception e) {
                    // Ignorieren falls altes Bild nicht existiert
                }
            }

            String filename = imageService.saveImage(image, product.getName());
            product.setImageUrl("/api/images/" + filename);
        }else if (updatedProduct.getImageUrl() == null && product.getImageUrl() != null) {
            if (product.getImageUrl().startsWith("/api/images/")) {
                String oldFilename = product.getImageUrl().replace("/api/images/", "");
                try {
                    imageService.deleteImage(oldFilename);
                } catch (Exception e) {
                    // Ignorieren
                }
            }
            product.setImageUrl(null);
        }
        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setExpiryDate(updatedProduct.getExpiryDate());
        product.setPrice(updatedProduct.getPrice());
        product.setAccessories(updatedProduct.getAccessories());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kategorie", categoryId));
            product.setCategory(category);
        }

        if (locationId != null) {
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));
            product.setLocation(location);
        }

        return productRepository.save(product);
    }

    // Product löschen (Soft-Delete)
    public void deleteProduct(Long id) {
        Product product = getProductById(id);

        // Bild löschen falls vorhanden
        if (product.getImageUrl() != null && product.getImageUrl().startsWith("/api/images/")) {
            String filename = product.getImageUrl().replace("/api/images/", "");
            try {
                imageService.deleteImage(filename);
            } catch (Exception e) {
                // Ignorieren falls Bild nicht existiert
            }
        }

        product.softDelete();
        productRepository.save(product);
    }
}