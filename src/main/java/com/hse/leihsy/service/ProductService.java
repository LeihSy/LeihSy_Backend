package com.hse.leihsy.service;

import com.hse.leihsy.model.dto.timePeriodDTO;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.ProductRepository;
import com.hse.leihsy.repository.CategoryRepository;
import com.hse.leihsy.repository.LocationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ImageService imageService;
    private final ItemService itemService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          LocationRepository locationRepository, ImageService imageService, ItemService itemService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.imageService = imageService;
        this.itemService = itemService;
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

    record BookingEvent(LocalDateTime bookingEventDate, int changeInLendedItems) {} // Für jeden Start / Ende einer Buchung, -1 heißt ein Item wird frei, +1 heißt ein Item wird belegt

    // Verfügbare Zeiträume eines Produkts laden
    public List<timePeriodDTO> getAvailablePeriods(Long id, int requiredQuantity) {

        List<Item> items = itemService.getItemsByProductId(id);

        // Prüfe ob genug Items existieren
        int totalItems = items.size();
        if(totalItems < requiredQuantity) {
            return List.of();
        }

        List<timePeriodDTO> availablePeriods = new ArrayList<>();

        // Zeitstrahl auf dem alle BookingEvents chronologisch aufgelistet werden
        List<BookingEvent> bookingEvents = new ArrayList<>();

        for (Item item : items) {

            // Entferne vergangene Bookings
            List<Booking> bookings = item.getBookings().stream().filter(booking -> booking.getEndDate().isAfter(LocalDateTime.now())).toList();

            for (Booking booking : bookings) {
                // Für jeden Start auf Zeitstrahl vermerken dass ein Item mehr in use ist
                bookingEvents.add(new BookingEvent(
                        booking.getStartDate(),
                        +1
                ));
                // Für jedes Ende auf Zeitstrahl vermerken dass ein Item weniger in use ist
                bookingEvents.add(new BookingEvent(
                        booking.getEndDate(),
                        -1
                ));
            }
        }

        // Sortiere BookingEvents auf Zeistrahl chronologisch
        bookingEvents.sort(Comparator.comparing(BookingEvent::bookingEventDate));

        int currentLendedItems = 0; // Misst die aktuell verliehene Anzahl an Items des Produkts
        LocalDateTime currentAvailableStart = null;

        for(BookingEvent bookingEvent : bookingEvents) {
            int freeItemsBeforeEvent = totalItems - currentLendedItems;
            boolean wasAvailableBefore = freeItemsBeforeEvent >= requiredQuantity;   // Prüfe ob vor BookingEvent ausreichend Items verfügbar waren

            currentLendedItems += bookingEvent.changeInLendedItems();   // Wende Verfügbarkeitsänderung auf Anzahl ausgeliehener Items an

            int freeItemsAfterEvent = totalItems - currentLendedItems;
            boolean isAvailableAfter = freeItemsAfterEvent >= requiredQuantity; // Prüfe ob nach BookingEvent noch ausreichend Items verfügbar sind

            // Wenn Produkt von nicht verfügbar auf verfügbar übergeht
            if(!wasAvailableBefore && isAvailableAfter) {
                currentAvailableStart = bookingEvent.bookingEventDate();    // Setze Startzeitraum des Items
            }
            // Wenn Produkt von verfügbar auf nicht verfügbar übergeht
            if(wasAvailableBefore && !isAvailableAfter && !(currentAvailableStart == null)) {
                availablePeriods.add(new timePeriodDTO(    // Füge neue Verfügbarkeitsperiode hinzu
                        currentAvailableStart,
                        bookingEvent.bookingEventDate()
                ));
                currentAvailableStart = null;
            }
        }

        // Zeitraum des letzten Events bis unendlich als verfügbare Zeit angeben
        if(!bookingEvents.isEmpty()) {
            BookingEvent lastBookingEvent = bookingEvents.get(bookingEvents.size() - 1);
            availablePeriods.add(new timePeriodDTO(
                    lastBookingEvent.bookingEventDate(),
                    null
            ));
        }


        // Falls keine Zeiträume vorhanden -> Item immer verfügbar
        if(availablePeriods.isEmpty()) {
            availablePeriods.add(new timePeriodDTO(
                    LocalDateTime.now(),
                    null
            ));
        }
        return availablePeriods;    // Gebe Liste verfügbarer Zeiträume des Produkts für die benötigte Quantität zurück
    }

    // Nicht verfügbare Zeiträume eines Produkts laden
    public List<timePeriodDTO> getUnavailablePeriods(Long id, int requiredQuantity) {

        List<Item> items = itemService.getItemsByProductId(id);

        int totalItems = items.size();

        List<timePeriodDTO> unavailablePeriods = new ArrayList<>();

        // Falls mehr Items angefragt werden als überhaupt im System vorhanden sind
        if (requiredQuantity > totalItems) {
            unavailablePeriods.add(new timePeriodDTO(
                            LocalDateTime.now(),
                            LocalDateTime.now().plusYears(1)
                    )
            );
            return unavailablePeriods;
        }

        // Zeitstrahl auf dem alle BookingEvents chronologisch aufgelistet werden
        List<BookingEvent> bookingEvents = new ArrayList<>();

        for (Item item : items) {

            // Entferne vergangene Bookings
            List<Booking> bookings = item.getBookings().stream().filter(booking -> booking.getEndDate().isAfter(LocalDateTime.now())).toList();

            for (Booking booking : bookings) {
                // Für jeden Start auf Zeitstrahl vermerken dass ein Item mehr in use ist
                bookingEvents.add(new BookingEvent(
                        booking.getStartDate(),
                        +1
                ));
                // Für jedes Ende auf Zeitstrahl vermerken dass ein Item weniger in use ist
                bookingEvents.add(new BookingEvent(
                        booking.getEndDate(),
                        -1
                ));
            }
        }

        // Sortiere BookingEvents auf Zeistrahl chronologisch
        bookingEvents.sort(Comparator.comparing(BookingEvent::bookingEventDate));

        int currentLendedItems = 0; // Misst die aktuell verliehene Anzahl an Items des Produkts
        LocalDateTime currentUnavailableStart = null;

        for(BookingEvent bookingEvent : bookingEvents) {
            int freeItemsBeforeEvent = totalItems - currentLendedItems;
            boolean wasUnavailableBefore = freeItemsBeforeEvent < requiredQuantity;   // Prüfe ob vor BookingEvent nicht ausreichend Items verfügbar waren

            currentLendedItems += bookingEvent.changeInLendedItems();   // Wende Verfügbarkeitsänderung auf Anzahl ausgeliehener Items an

            int freeItemsAfterEvent = totalItems - currentLendedItems;
            boolean isUnavailableAfter = freeItemsAfterEvent < requiredQuantity; // Prüfe ob nach BookingEvent noch nicht ausreichend Items verfügbar sind

            // Wenn Produkt von verfügbar auf nicht verfügbar übergeht
            if(!wasUnavailableBefore && isUnavailableAfter) {
                currentUnavailableStart = bookingEvent.bookingEventDate();    // Setze Startzeitraum des nicht verfügbaren Zeitraums
                if(currentUnavailableStart.isBefore(LocalDateTime.now())) {
                    currentUnavailableStart = LocalDateTime.now();
                }
            }
            // Wenn Produkt von nicht verfügbar auf verfügbar übergeht
            if(wasUnavailableBefore && !isUnavailableAfter && !(currentUnavailableStart == null)) {
                unavailablePeriods.add(new timePeriodDTO(    // Füge neue Nichtverfügbarkeitsperiode hinzu
                        currentUnavailableStart,
                        bookingEvent.bookingEventDate()
                ));
                currentUnavailableStart = null;
            }
        }
        return unavailablePeriods;    // Gebe Liste verfügbarer Zeiträume des Produkts für die benötigte Quantität zurück
    }
}