package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.config.TestSecurityConfig;
import com.hse.leihsy.config.UserSyncFilter;
import com.hse.leihsy.model.dto.*;
import com.hse.leihsy.model.entity.InsyImportStatus;
import com.hse.leihsy.service.InsyImportService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InsyImportController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("InsyImportController Functional Tests")
class InsyImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InsyImportService importService;

    @MockitoBean
    private UserSyncFilter userSyncFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Filter Chain Bypass
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userSyncFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/insy/imports - Sollte Liste zurückgeben")
    void getAllImports_ShouldReturnList() throws Exception {
        // Arrange
        InsyImportItemDTO item = new InsyImportItemDTO();
        item.setId(1L);
        item.setName("Test Item");

        when(importService.getAll(any())).thenReturn(List.of(item));

        // Act & Assert
        mockMvc.perform(get("/api/insy/imports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/insy/imports?status=PENDING - Sollte Filter verwenden")
    void getAllImports_Pending_ShouldCallPendingService() throws Exception {
        // Arrange
        when(importService.getAllPending()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/insy/imports").param("status", "PENDING"))
                .andExpect(status().isOk());

        verify(importService).getAllPending();
    }

    @Test
    @DisplayName("POST /api/insy/imports - Sollte Push-Daten empfangen (201 Created)")
    void receiveFromInsy_ShouldReturnCreated() throws Exception {
        // Arrange
        InsyImportPushDTO pushDTO = new InsyImportPushDTO();
        pushDTO.setInsyId(12345L);
        pushDTO.setName("Push Item");

        // Service gibt Entity zurück, Controller holt dann DTO
        // Wir simulieren hier den Ablauf im Controller
        com.hse.leihsy.model.entity.InsyImportItem savedItem = new com.hse.leihsy.model.entity.InsyImportItem();
        savedItem.setId(10L);

        InsyImportItemDTO responseDTO = new InsyImportItemDTO();
        responseDTO.setId(10L);
        responseDTO.setInsyId(12345L);

        when(importService.receiveFromInsy(any(InsyImportPushDTO.class))).thenReturn(savedItem);
        when(importService.getById(10L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/insy/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("PATCH /api/insy/imports/{id} - REJECT Action")
    void updateImportStatus_Reject_ShouldReturnOk() throws Exception {
        // Arrange
        InsyImportStatusUpdateDTO updateDTO = new InsyImportStatusUpdateDTO();
        updateDTO.setAction(InsyImportStatusUpdateDTO.Action.REJECT);
        updateDTO.setRejectReason("Duplicate");

        InsyImportItemDTO responseDTO = new InsyImportItemDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(InsyImportStatus.REJECTED);

        when(importService.rejectItem(any(InsyRejectRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/insy/imports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(importService).rejectItem(any(InsyRejectRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/insy/imports/{id} - IMPORT Action")
    void updateImportStatus_Import_ShouldReturnOk() throws Exception {
        // Arrange
        InsyImportStatusUpdateDTO updateDTO = new InsyImportStatusUpdateDTO();
        updateDTO.setAction(InsyImportStatusUpdateDTO.Action.IMPORT);
        updateDTO.setImportType(InsyImportRequestDTO.ImportType.NEW_PRODUCT);
        updateDTO.setInvNumber("INV-001");

        InsyImportItemDTO responseDTO = new InsyImportItemDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(InsyImportStatus.IMPORTED);

        when(importService.importItem(any(InsyImportRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/insy/imports/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IMPORTED"));

        verify(importService).importItem(any(InsyImportRequestDTO.class));
    }

    @Test
    @DisplayName("PATCH /api/insy/imports/batch - Sollte Batch Import ausführen")
    void batchUpdateStatus_ShouldReturnList() throws Exception {
        // Arrange
        InsyBatchImportRequestDTO batchDTO = new InsyBatchImportRequestDTO();
        batchDTO.setImportItemIds(List.of(1L, 2L));
        batchDTO.setProductId(5L);

        when(importService.batchImport(any(InsyBatchImportRequestDTO.class)))
                .thenReturn(List.of(new InsyImportItemDTO(), new InsyImportItemDTO()));

        // Act & Assert
        mockMvc.perform(patch("/api/insy/imports/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/insy/imports/count - Sollte Count zurückgeben")
    void getImportCount_ShouldReturnMap() throws Exception {
        // Arrange
        when(importService.countPending()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/insy/imports/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }
}