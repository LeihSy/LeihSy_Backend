package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.config.SecurityConfig;
import com.hse.leihsy.config.UserSyncFilter;
import com.hse.leihsy.mapper.ItemMapper;
import com.hse.leihsy.mapper.ProductMapper;
import com.hse.leihsy.service.ItemService;
import com.hse.leihsy.service.ProductService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("security-integration")
@DisplayName("Controller Security Integration Tests")
class ControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // --- SECURITY MOCKS ---
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private UserSyncFilter userSyncFilter;

    // --- CONTROLLER DEPENDENCIES ---
    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private ItemMapper itemMapper;

    // Diese Beans werden vom Controller Konstruktor benötigt
    @Autowired
    private Validator validator;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() throws ServletException, IOException {
        // WICHTIG: Der UserSyncFilter Mock muss die Chain weiterführen!
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userSyncFilter).doFilter(any(), any(), any());

        // Standard-Stubs für GET Requests
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        when(productMapper.toDTOList(any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/products sollte öffentlich sein (200 OK)")
    void getProducts_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} sollte ohne Auth 401 Unauthorized liefern")
    void deleteProduct_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} sollte mit User-Rolle 403 Forbidden liefern")
    void deleteProduct_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} sollte mit falscher Rolle (Lender) 403 Forbidden liefern")
    void deleteProduct_WithLenderRole_ShouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_LENDER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} sollte mit Admin-Rolle 204 NoContent liefern")
    void deleteProduct_WithAdminRole_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}