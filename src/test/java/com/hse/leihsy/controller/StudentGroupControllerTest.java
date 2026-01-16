package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.config.TestSecurityConfig;
import com.hse.leihsy.config.UserSyncFilter;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.model.dto.CreateStudentGroupDTO;
import com.hse.leihsy.model.dto.StudentGroupDTO;
import com.hse.leihsy.service.StudentGroupService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentGroupController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("StudentGroupController Functional Tests")
class StudentGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentGroupService groupService;

    @MockitoBean
    private UserSyncFilter userSyncFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Filter Chain Bypass für UserSyncFilter
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userSyncFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/groups - Sollte 201 Created liefern bei valider Gruppe")
    void createGroup_Valid_ShouldReturn201() throws Exception {
        // Arrange
        CreateStudentGroupDTO createDTO = new CreateStudentGroupDTO();
        createDTO.setName("VR Projektgruppe");
        createDTO.setDescription("Wir drehen einen Film");
        createDTO.setMemberIds(List.of(2L, 3L));

        StudentGroupDTO responseDTO = new StudentGroupDTO();
        responseDTO.setId(10L);
        responseDTO.setName("VR Projektgruppe");
        responseDTO.setMemberCount(3);

        when(groupService.createGroup(any(CreateStudentGroupDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("VR Projektgruppe"));
    }

    @Test
    @DisplayName("POST /api/groups - Sollte 400 Bad Request bei ungültigem Namen liefern")
    void createGroup_Invalid_ShouldReturn400() throws Exception {
        // Arrange
        CreateStudentGroupDTO invalidDTO = new CreateStudentGroupDTO();
        invalidDTO.setName("A");

        // Act & Assert
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(groupService, never()).createGroup(any());
    }

    @Test
    @DisplayName("GET /api/groups/{id} - Sollte 200 OK liefern")
    void getGroupById_ShouldReturnGroup() throws Exception {
        // Arrange
        StudentGroupDTO dto = new StudentGroupDTO();
        dto.setId(10L);
        dto.setName("Test Group");

        when(groupService.getGroupById(10L)).thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/api/groups/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Group"));
    }

    @Test
    @DisplayName("GET /api/groups/{id} - Sollte 404 Not Found liefern")
    void getGroupById_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(groupService.getGroupById(99L))
                .thenThrow(new ResourceNotFoundException("StudentGroup", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/groups/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/groups/{groupId}/members/{userId} - Sollte 200 OK liefern")
    void addMember_ShouldReturnUpdatedGroup() throws Exception {
        // Arrange
        StudentGroupDTO updatedGroup = new StudentGroupDTO();
        updatedGroup.setId(10L);
        updatedGroup.setMemberCount(5);

        when(groupService.addMember(10L, 5L)).thenReturn(updatedGroup);

        // Act & Assert
        mockMvc.perform(post("/api/groups/10/members/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(5));
    }

    @Test
    @DisplayName("GET /api/groups?q=... - Sollte Suche ausführen")
    void searchGroups_ShouldReturnResults() throws Exception {
        // Arrange
        StudentGroupDTO group = new StudentGroupDTO();
        group.setName("Found Me");
        when(groupService.searchGroups("Found")).thenReturn(List.of(group));

        // Act & Assert
        mockMvc.perform(get("/api/groups").param("q", "Found"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Found Me"));

        verify(groupService).searchGroups("Found");
    }

    @Test
    @DisplayName("DELETE /api/groups/{id} - Sollte 204 No Content liefern")
    void deleteGroup_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(groupService).deleteGroup(10L);

        // Act & Assert
        mockMvc.perform(delete("/api/groups/10"))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(10L);
    }
}