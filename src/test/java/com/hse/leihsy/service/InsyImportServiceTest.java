package com.hse.leihsy.service;

import com.hse.leihsy.exception.ConflictException;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.exception.ValidationException;
import com.hse.leihsy.mapper.InsyImportMapper;
import com.hse.leihsy.model.dto.*;
import com.hse.leihsy.model.entity.*;
import com.hse.leihsy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InsyImportService Tests")
class InsyImportServiceTest {

    @Mock
    private InsyImportItemRepository importRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InsyImportMapper importMapper;

    @InjectMocks
    private InsyImportService importService;

    private InsyImportItem pendingItem;
    private InsyImportPushDTO pushDTO;

    @BeforeEach
    void setUp() {
        // Basis-Setup für einen PENDING Eintrag
        pendingItem = InsyImportItem.builder()
                .insyId(1001L)
                .name("Old Name")
                .description("Old Desc")
                .status(InsyImportStatus.PENDING)
                .invNumber("OLD-001")
                .build();

        // ID manuell setzen via Setter (BaseEntity hat Setter)
        pendingItem.setId(1L);

        // Basis-Setup für einen eingehenden Push
        pushDTO = new InsyImportPushDTO();
        pushDTO.setInsyId(1001L);
        pushDTO.setName("New Name");
        pushDTO.setDescription("New Desc");
        pushDTO.setInvNumber("NEW-001");
    }

    @Nested
    @DisplayName("receiveFromInsy Tests")
    class ReceiveFromInsyTests {

        @Test
        @DisplayName("Sollte neues Item erstellen, wenn ID unbekannt")
        void shouldCreateNewItemWhenIdUnknown() {
            // Arrange
            when(importRepository.findByInsyId(1001L)).thenReturn(Optional.empty());
            // Mock Save: Gibt das Objekt zurück, das gespeichert wird
            when(importRepository.save(any(InsyImportItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            InsyImportItem result = importService.receiveFromInsy(pushDTO);

            // Assert
            assertThat(result.getStatus()).isEqualTo(InsyImportStatus.PENDING);
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getInsyId()).isEqualTo(1001L);
            verify(importRepository).save(any(InsyImportItem.class));
        }

        @Test
        @DisplayName("Sollte existierendes PENDING Item aktualisieren (Update im Staging)")
        void shouldUpdateExistingPendingItem() {
            // Arrange
            when(importRepository.findByInsyId(1001L)).thenReturn(Optional.of(pendingItem));
            when(importRepository.save(any(InsyImportItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            InsyImportItem result = importService.receiveFromInsy(pushDTO);

            // Assert
            assertThat(result.getName()).isEqualTo("New Name"); // Name aktualisiert
            assertThat(result.getDescription()).isEqualTo("New Desc");
            assertThat(result.getStatus()).isEqualTo(InsyImportStatus.PENDING); // Status bleibt PENDING
            verify(importRepository).save(pendingItem);
        }

        @Test
        @DisplayName("Sollte ConflictException werfen, wenn Item schon IMPORTED")
        void shouldThrowConflictWhenAlreadyImported() {
            // Arrange
            pendingItem.setStatus(InsyImportStatus.IMPORTED);
            when(importRepository.findByInsyId(1001L)).thenReturn(Optional.of(pendingItem));

            // Act & Assert
            assertThatThrownBy(() -> importService.receiveFromInsy(pushDTO))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("bereits verarbeitet");

            // Sicherstellen, dass nichts gespeichert wurde
            verify(importRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("importItem Tests")
    class ImportItemTests {

        private InsyImportRequestDTO importRequest;
        private Product productMock;
        private Item itemMock;

        @BeforeEach
        void setUpImport() {
            // DTO Setup
            importRequest = new InsyImportRequestDTO();
            importRequest.setImportItemId(1L);
            importRequest.setInvNumber("INV-NEW");
            importRequest.setImportType(InsyImportRequestDTO.ImportType.NEW_PRODUCT);

            // Entity Mocks
            productMock = new Product();
            productMock.setId(10L);
            productMock.setName("New Product");

            itemMock = new Item();
            itemMock.setId(20L);
            itemMock.setInvNumber("INV-NEW");
        }

        @Test
        @DisplayName("Sollte als neues Produkt importieren (NEW_PRODUCT)")
        void shouldImportAsNewProduct() {
            // Arrange
            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));
            when(itemRepository.findByInvNumber("INV-NEW")).thenReturn(Optional.empty()); // InvNummer ist frei

            // Request Details
            importRequest.setImportType(InsyImportRequestDTO.ImportType.NEW_PRODUCT);
            importRequest.setCategoryId(5L);
            importRequest.setLocationId(6L);
            importRequest.setPrice(new BigDecimal("99.99"));

            // Repository Mocks
            when(categoryRepository.findById(5L)).thenReturn(Optional.of(new Category()));
            when(locationRepository.findById(6L)).thenReturn(Optional.of(new Location()));
            when(productRepository.save(any(Product.class))).thenReturn(productMock);
            when(itemRepository.save(any(Item.class))).thenReturn(itemMock);

            // Mapper
            when(importMapper.toDTO(any())).thenReturn(new InsyImportItemDTO());

            // Act
            importService.importItem(importRequest);

            // Assert
            assertThat(pendingItem.getStatus()).isEqualTo(InsyImportStatus.IMPORTED);
            assertThat(pendingItem.getImportedProduct()).isEqualTo(productMock);

            verify(productRepository).save(any(Product.class));
            verify(itemRepository).save(any(Item.class));
            verify(importRepository).save(pendingItem);
        }

        @Test
        @DisplayName("Sollte zu existierendem Produkt hinzufügen (EXISTING_PRODUCT)")
        void shouldImportToExistingProduct() {
            // Arrange
            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));
            when(itemRepository.findByInvNumber("INV-NEW")).thenReturn(Optional.empty());

            // Request Details
            importRequest.setImportType(InsyImportRequestDTO.ImportType.EXISTING_PRODUCT);
            importRequest.setExistingProductId(10L);

            // Repository Mocks
            when(productRepository.findById(10L)).thenReturn(Optional.of(productMock));
            when(itemRepository.save(any(Item.class))).thenReturn(itemMock);
            when(importMapper.toDTO(any())).thenReturn(new InsyImportItemDTO());

            // Act
            importService.importItem(importRequest);

            // Assert
            assertThat(pendingItem.getStatus()).isEqualTo(InsyImportStatus.IMPORTED);
            assertThat(pendingItem.getImportedItem()).isEqualTo(itemMock);

            // Prüfen ob Item korrekt dem existierenden Produkt zugeordnet wurde
            verify(itemRepository).save(argThat(item -> item.getProduct().equals(productMock)));
        }

        @Test
        @DisplayName("Sollte existierendes Item aktualisieren wenn InvNumber schon existiert (Update Logic)")
        void shouldUpdateExistingItemIfInvNumberExists() {
            // Arrange
            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));
            // InvNumber existiert bereits -> Update Fall
            when(itemRepository.findByInvNumber("INV-NEW")).thenReturn(Optional.of(itemMock));

            when(itemRepository.save(any(Item.class))).thenReturn(itemMock);
            when(importMapper.toDTO(any())).thenReturn(new InsyImportItemDTO());

            // Act
            importService.importItem(importRequest);

            // Assert
            assertThat(pendingItem.getStatus()).isEqualTo(InsyImportStatus.UPDATED);
            assertThat(pendingItem.getImportedItem()).isEqualTo(itemMock);

            verify(itemRepository).save(itemMock); // Altes Item updated
            verify(importRepository).save(pendingItem);
        }

        @Test
        @DisplayName("Sollte Fehler werfen, wenn Import-Eintrag nicht gefunden")
        void shouldThrowWhenImportItemNotFound() {
            when(importRepository.findById(999L)).thenReturn(Optional.empty());
            importRequest.setImportItemId(999L);

            assertThatThrownBy(() -> importService.importItem(importRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Sollte Fehler werfen, wenn Status nicht PENDING (Validation)")
        void shouldThrowWhenStatusNotPending() {
            pendingItem.setStatus(InsyImportStatus.REJECTED);
            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));

            assertThatThrownBy(() -> importService.importItem(importRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("kann nicht importiert werden");
        }
    }

    @Nested
    @DisplayName("rejectItem Tests")
    class RejectItemTests {

        @Test
        @DisplayName("Sollte Item ablehnen und Grund speichern")
        void shouldRejectItem() {
            // Arrange
            InsyRejectRequestDTO rejectRequest = new InsyRejectRequestDTO(1L, "Duplicate");

            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));
            when(importMapper.toDTO(any())).thenReturn(new InsyImportItemDTO());

            // Act
            importService.rejectItem(rejectRequest);

            // Assert
            assertThat(pendingItem.getStatus()).isEqualTo(InsyImportStatus.REJECTED);
            assertThat(pendingItem.getAdminNote()).isEqualTo("Duplicate");
            verify(importRepository).save(pendingItem);
        }

        @Test
        @DisplayName("Sollte Conflict werfen, wenn bereits verarbeitet")
        void shouldThrowConflictWhenAlreadyProcessed() {
            // Arrange
            pendingItem.setStatus(InsyImportStatus.IMPORTED);
            InsyRejectRequestDTO rejectRequest = new InsyRejectRequestDTO(1L, "Reason");

            when(importRepository.findById(1L)).thenReturn(Optional.of(pendingItem));

            // Act & Assert
            assertThatThrownBy(() -> importService.rejectItem(rejectRequest))
                    .isInstanceOf(ConflictException.class);
        }
    }
}