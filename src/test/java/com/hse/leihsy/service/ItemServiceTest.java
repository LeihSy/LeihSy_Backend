package com.hse.leihsy.service;

import com.hse.leihsy.model.entity.Item;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.repository.ItemRepository;
import com.hse.leihsy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        // Service mit gemockten Repositories initialisieren
        itemService = new ItemService(itemRepository, productRepository);
    }

    @Test
    void createItemSet_ShouldCreateCorrectNumberOfItems() {
        // ARRANGE (Vorbereitung)
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);

        // Mock: Produkt wird gefunden
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Mock: FindByInvNumber gibt immer "leer" zurück (d.h. Nummern sind noch frei)
        when(itemRepository.findByInvNumber(anyString())).thenReturn(Optional.empty());

        // Mock: Save gibt einfach das Item zurück, das reingesteckt wurde
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        // ACT (Ausführung)
        // Wir wollen 5 Items mit dem Präfix "VR" erstellen
        List<Item> results = itemService.createItemSet("VR", "Owner", productId, 5);

        // ASSERT (Überprüfung)
        assertEquals(5, results.size(), "Es sollten genau 5 Items erstellt werden");

        // Prüfen ob die Nummerierung korrekt bei 001 startet
        assertEquals("VR-001", results.get(0).getInvNumber());
        assertEquals("VR-005", results.get(4).getInvNumber());

        // Verifiziere, dass save() genau 5 mal aufgerufen wurde
        verify(itemRepository, times(5)).save(any(Item.class));
    }

    @Test
    void createItemSet_ShouldSkipExistingNumbers() {
        // Testet die Logik: Wenn VR-001 schon existiert, muss VR-002 erstellt werden

        // ARRANGE
        Long productId = 1L;
        Product mockProduct = new Product();

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

        // SIMULATION: "VR-001" und "VR-002" existieren bereits in der Datenbank!
        when(itemRepository.findByInvNumber("VR-001")).thenReturn(Optional.of(new Item()));
        when(itemRepository.findByInvNumber("VR-002")).thenReturn(Optional.of(new Item()));

        // Ab VR-003 ist alles frei
        when(itemRepository.findByInvNumber("VR-003")).thenReturn(Optional.empty());
        when(itemRepository.findByInvNumber("VR-004")).thenReturn(Optional.empty());
        when(itemRepository.findByInvNumber("VR-005")).thenReturn(Optional.empty());

        // ACT
        // Wir fordern 3 NEUE Items an
        List<Item> results = itemService.createItemSet("VR", "Owner", productId, 3);

        // ASSERT
        assertEquals(3, results.size(), "Es sollen trotzdem 3 neue Items entstehen");

        // Das erste neue Item muss VR-003 sein (da 001 und 002 besetzt waren)
        assertEquals("VR-003", results.get(0).getInvNumber());
        assertEquals("VR-004", results.get(1).getInvNumber());
        assertEquals("VR-005", results.get(2).getInvNumber());
    }
}