package com.hse.leihsy.service;

import com.hse.leihsy.exception.ConflictException;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.exception.ValidationException;
import com.hse.leihsy.mapper.InsyImportMapper;
import com.hse.leihsy.model.dto.*;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InsyImportService {

    private final InsyImportItemRepository importRepository;
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final InsyImportMapper importMapper;

    // ========================================
    // RECEIVE DATA FROM INSY
    // ========================================

    /**
     * Empfaengt Daten von InSy und erstellt/aktualisiert Import-Eintrag.
     * Bei bestehender InSy-ID wird der Eintrag aktualisiert (wenn noch PENDING).
     */
    public InsyImportItem receiveFromInsy(InsyImportPushDTO pushData) {
        log.info("Receiving data from InSy: insyId={}, name={}", pushData.getInsyId(), pushData.getName());

        // Pruefen ob Eintrag mit dieser InSy-ID bereits existiert
        Optional<InsyImportItem> existing = importRepository.findByInsyId(pushData.getInsyId());

        if (existing.isPresent()) {
            InsyImportItem item = existing.get();

            // Nur aktualisieren wenn noch PENDING
            if (item.getStatus() == InsyImportStatus.PENDING) {
                log.info("Updating existing PENDING import item: id={}", item.getId());
                item.setName(pushData.getName());
                item.setDescription(pushData.getDescription());
                item.setLocation(pushData.getLocation());
                item.setOwner(pushData.getOwner());
                item.setInvNumber(pushData.getInvNumber());
                return importRepository.save(item);
            } else {
                log.info("Import item already processed, creating note: id={}, status={}", item.getId(), item.getStatus());
                throw new ConflictException("InSy-Eintrag mit ID " + pushData.getInsyId() +
                        " wurde bereits verarbeitet (Status: " + item.getStatus() + ")");
            }
        }

        // Neuen Eintrag erstellen
        InsyImportItem newItem = InsyImportItem.builder()
                .insyId(pushData.getInsyId())
                .name(pushData.getName())
                .description(pushData.getDescription())
                .location(pushData.getLocation())
                .owner(pushData.getOwner())
                .invNumber(pushData.getInvNumber())
                .status(InsyImportStatus.PENDING)
                .build();

        return importRepository.save(newItem);
    }

    /**
     * Empfaengt mehrere Eintraege von InSy (Bulk-Push)
     */
    public List<InsyImportItem> receiveMultipleFromInsy(List<InsyImportPushDTO> pushDataList) {
        List<InsyImportItem> results = new ArrayList<>();
        for (InsyImportPushDTO pushData : pushDataList) {
            try {
                results.add(receiveFromInsy(pushData));
            } catch (Exception e) {
                log.warn("Failed to receive InSy item: insyId={}, error={}", pushData.getInsyId(), e.getMessage());
            }
        }
        return results;
    }

    // ========================================
    // GET IMPORT ITEMS
    // ========================================

    /**
     * Alle PENDING Eintraege fuer Admin-Dashboard
     */
    public List<InsyImportItemDTO> getAllPending() {
        List<InsyImportItem> items = importRepository.findAllPending();
        return enrichWithMatchingProducts(items);
    }

    /**
     * Alle Eintraege (mit Status-Filter)
     */
    public List<InsyImportItemDTO> getAll(InsyImportStatus status) {
        List<InsyImportItem> items;
        if (status != null) {
            items = importRepository.findByStatus(status);
        } else {
            items = importRepository.findAllActive();
        }
        return enrichWithMatchingProducts(items);
    }

    /**
     * Einzelnen Eintrag abrufen
     */
    public InsyImportItemDTO getById(Long id) {
        InsyImportItem item = importRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import-Eintrag", id));
        return enrichWithMatchingProduct(importMapper.toDTO(item), item);
    }

    /**
     * Anzahl PENDING Eintraege (fuer Badge)
     */
    public long countPending() {
        return importRepository.countPending();
    }

    // ========================================
    // IMPORT ACTIONS
    // ========================================

    /**
     * Importiert einen Eintrag als neues Product + Item ODER als Item zu bestehendem Product.
     */
    public InsyImportItemDTO importItem(InsyImportRequestDTO request) {
        InsyImportItem importItem = importRepository.findById(request.getImportItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Import-Eintrag nicht gefunden: " + request.getImportItemId()));

        if (!importItem.canBeImported()) {
            throw new ValidationException("Import-Eintrag kann nicht importiert werden. Status: " + importItem.getStatus());
        }

        // Inventarnummer bestimmen (Request > InSy-Wert)
        String invNumber = request.getInvNumber() != null ? request.getInvNumber() : importItem.getInvNumber();
        if (invNumber == null || invNumber.isBlank()) {
            throw new ValidationException("Inventarnummer ist erforderlich");
        }

        // Pruefen ob Item mit dieser Inventarnummer bereits existiert
        Optional<Item> existingItem = itemRepository.findByInvNumber(invNumber);
        if (existingItem.isPresent()) {
            // Update statt Duplikat
            return updateExistingItem(importItem, existingItem.get(), request);
        }

        // Neues Item erstellen
        if (request.getImportType() == InsyImportRequestDTO.ImportType.NEW_PRODUCT) {
            return importAsNewProduct(importItem, request, invNumber);
        } else {
            return importToExistingProduct(importItem, request, invNumber);
        }
    }

    /**
     * Batch-Import: Mehrere Eintraege zu einem Product hinzufuegen
     */
    public List<InsyImportItemDTO> batchImport(InsyBatchImportRequestDTO request) {
        // Product laden und validieren
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        // Alle Import-Items laden
        List<InsyImportItem> importItems = importRepository.findPendingByIds(request.getImportItemIds());

        if (importItems.size() != request.getImportItemIds().size()) {
            throw new ValidationException("Nicht alle Import-Eintraege gefunden oder nicht im Status PENDING");
        }

        // Lender laden falls angegeben
        User lender = null;
        if (request.getLenderId() != null) {
            lender = userRepository.findById(request.getLenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lender", request.getLenderId()));
        }

        List<InsyImportItemDTO> results = new ArrayList<>();
        int counter = 1;

        for (InsyImportItem importItem : importItems) {
            // Inventarnummer generieren oder aus InSy uebernehmen
            String invNumber;
            if (request.getInvNumberPrefix() != null && !request.getInvNumberPrefix().isBlank()) {
                invNumber = generateNextInvNumber(request.getInvNumberPrefix());
            } else if (importItem.getInvNumber() != null && !importItem.getInvNumber().isBlank()) {
                invNumber = importItem.getInvNumber();
            } else {
                invNumber = product.getName().replaceAll("\\s+", "-").toUpperCase() + "-" + String.format("%03d", counter);
            }

            // Pruefen ob Item mit dieser Inventarnummer bereits existiert
            Optional<Item> existingItem = itemRepository.findByInvNumber(invNumber);
            if (existingItem.isPresent()) {
                // Update
                Item item = existingItem.get();
                item.setOwner(importItem.getOwner());
                item.setInsyId(importItem.getInsyId());
                if (lender != null) {
                    item.setLender(lender);
                }
                itemRepository.save(item);
                importItem.markAsUpdated(item);
            } else {
                // Neues Item erstellen
                Item newItem = Item.builder()
                        .invNumber(invNumber)
                        .owner(importItem.getOwner())
                        .insyId(importItem.getInsyId())
                        .product(product)
                        .lender(lender)
                        .build();
                newItem = itemRepository.save(newItem);
                importItem.markAsImportedToExistingProduct(newItem);
            }

            importRepository.save(importItem);
            results.add(importMapper.toDTO(importItem));
            counter++;
        }

        log.info("Batch import completed: {} items imported to product {}", results.size(), product.getName());
        return results;
    }

    /**
     * Lehnt einen Import-Eintrag ab
     */
    public InsyImportItemDTO rejectItem(InsyRejectRequestDTO request) {
        InsyImportItem importItem = importRepository.findById(request.getImportItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Import-Eintrag nicht gefunden: " + request.getImportItemId()));

        if (!importItem.canBeImported()) {
            throw new ConflictException("Import-Eintrag wurde bereits verarbeitet. Status: " + importItem.getStatus());
        }

        importItem.markAsRejected(request.getReason());
        importRepository.save(importItem);

        log.info("Import item rejected: id={}, reason={}", importItem.getId(), request.getReason());
        return importMapper.toDTO(importItem);
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Importiert als neues Product + Item
     */
    private InsyImportItemDTO importAsNewProduct(InsyImportItem importItem, InsyImportRequestDTO request, String invNumber) {
        // Kategorie und Location laden
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kategorie", request.getCategoryId()));
        }

        Location location = null;
        if (request.getLocationId() != null) {
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", request.getLocationId()));
        }

        User lender = null;
        if (request.getLenderId() != null) {
            lender = userRepository.findById(request.getLenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lender", request.getLenderId()));
        }

        // Neues Product erstellen
        Product newProduct = new Product();
        newProduct.setInsyId(importItem.getInsyId());
        newProduct.setName(importItem.getName());
        newProduct.setDescription(importItem.getDescription());
        newProduct.setCategory(category);
        newProduct.setLocation(location);
        newProduct.setPrice(request.getPrice());
        newProduct.setExpiryDate(request.getExpiryDate());
        newProduct.setIsActive(true);
        newProduct = productRepository.save(newProduct);

        // Neues Item erstellen
        Item newItem = Item.builder()
                .invNumber(invNumber)
                .owner(importItem.getOwner())
                .insyId(importItem.getInsyId())
                .product(newProduct)
                .lender(lender)
                .build();
        newItem = itemRepository.save(newItem);

        // Import-Eintrag aktualisieren
        importItem.markAsImportedWithProduct(newProduct, newItem);
        importRepository.save(importItem);

        log.info("Imported as new product: productId={}, itemId={}", newProduct.getId(), newItem.getId());
        return importMapper.toDTO(importItem);
    }

    /**
     * Importiert als Item zu bestehendem Product
     */
    private InsyImportItemDTO importToExistingProduct(InsyImportItem importItem, InsyImportRequestDTO request, String invNumber) {
        if (request.getExistingProductId() == null) {
            throw new ValidationException("Product-ID ist erforderlich fuer Import zu bestehendem Product");
        }

        Product product = productRepository.findById(request.getExistingProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getExistingProductId()));

        User lender = null;
        if (request.getLenderId() != null) {
            lender = userRepository.findById(request.getLenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lender", request.getLenderId()));
        }

        // Neues Item erstellen
        Item newItem = Item.builder()
                .invNumber(invNumber)
                .owner(importItem.getOwner())
                .insyId(importItem.getInsyId())
                .product(product)
                .lender(lender)
                .build();
        newItem = itemRepository.save(newItem);

        // Import-Eintrag aktualisieren
        importItem.markAsImportedToExistingProduct(newItem);
        importRepository.save(importItem);

        log.info("Imported to existing product: productId={}, itemId={}", product.getId(), newItem.getId());
        return importMapper.toDTO(importItem);
    }

    /**
     * Aktualisiert ein bestehendes Item (bei gleicher Inventarnummer)
     */
    private InsyImportItemDTO updateExistingItem(InsyImportItem importItem, Item existingItem, InsyImportRequestDTO request) {
        log.info("Updating existing item with invNumber: {}", existingItem.getInvNumber());

        // Item aktualisieren
        existingItem.setOwner(importItem.getOwner());
        existingItem.setInsyId(importItem.getInsyId());

        if (request.getLenderId() != null) {
            User lender = userRepository.findById(request.getLenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lender", request.getLenderId()));
            existingItem.setLender(lender);
        }

        itemRepository.save(existingItem);

        // Import-Eintrag als Update markieren
        importItem.markAsUpdated(existingItem);
        importRepository.save(importItem);

        return importMapper.toDTO(importItem);
    }

    /**
     * Generiert die naechste verfuegbare Inventarnummer fuer ein Prefix
     */
    private String generateNextInvNumber(String prefix) {
        int counter = 1;
        String invNumber;
        do {
            invNumber = prefix + "-" + String.format("%03d", counter);
            counter++;
        } while (itemRepository.findByInvNumber(invNumber).isPresent() && counter < 1000);

        if (counter >= 1000) {
            throw new RuntimeException("Konnte keine freie Inventarnummer fuer Prefix " + prefix + " finden");
        }
        return invNumber;
    }

    /**
     * Reichert DTOs mit Matching-Product-Info an
     */
    private List<InsyImportItemDTO> enrichWithMatchingProducts(List<InsyImportItem> items) {
        List<InsyImportItemDTO> dtos = new ArrayList<>();
        for (InsyImportItem item : items) {
            InsyImportItemDTO dto = importMapper.toDTO(item);
            enrichWithMatchingProduct(dto, item);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Reichert ein DTO mit Matching-Product-Info an
     */
    private InsyImportItemDTO enrichWithMatchingProduct(InsyImportItemDTO dto, InsyImportItem item) {
        // Suche nach Product mit gleichem oder aehnlichem Namen
        List<Product> matchingProducts = productRepository.searchByName(item.getName());

        if (!matchingProducts.isEmpty()) {
            Product match = matchingProducts.getFirst();
            dto.setHasMatchingProduct(true);
            dto.setMatchingProductId(match.getId());
            dto.setMatchingProductName(match.getName());
        } else {
            dto.setHasMatchingProduct(false);
        }

        return dto;
    }
}