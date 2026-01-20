package com.hse.leihsy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hse.leihsy.config.TestSecurityConfig;
import com.hse.leihsy.config.UserSyncFilter;
import com.hse.leihsy.exception.ResourceNotFoundException;
import com.hse.leihsy.mapper.ItemMapper;
import com.hse.leihsy.mapper.ProductMapper;
import com.hse.leihsy.model.dto.ProductCreateDTO;
import com.hse.leihsy.model.dto.ProductDTO;
import com.hse.leihsy.model.entity.Product;
import com.hse.leihsy.service.ItemService;
import com.hse.leihsy.service.ProductService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ProductController Functional Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private ItemMapper itemMapper;

    @MockitoBean
    private UserSyncFilter userSyncFilter;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userSyncFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Sollte 200 OK und Produkt zurückgeben")
    void getProduct_ShouldReturn200() throws Exception {
        // Arrange
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Test Product");

        when(productService.getProductById(1L)).thenReturn(new Product());
        when(productMapper.toDTO(any())).thenReturn(productDTO);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Sollte 404 Not Found liefern wenn ID unbekannt")
    void getProduct_NotFound_ShouldReturn404() throws Exception {
        // Arrange
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product", 99L));

        // Act & Assert
        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products - Sollte 201 Created liefern bei validem Input")
    void createProduct_Valid_ShouldReturn201() throws Exception {
        // Arrange
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("New VR Headset");
        createDTO.setDescription("Cool stuff");
        createDTO.setCategoryId(1L);
        createDTO.setLocationId(2L);

        String dtoJson = objectMapper.writeValueAsString(createDTO);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", dtoJson.getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image".getBytes());

        Product savedProduct = new Product();
        savedProduct.setId(10L);
        savedProduct.setName("New VR Headset");

        ProductDTO responseDTO = new ProductDTO();
        responseDTO.setId(10L);
        responseDTO.setName("New VR Headset");

        when(productService.createProduct(any(Product.class), eq(1L), eq(2L), any())).thenReturn(savedProduct);
        when(productMapper.toDTO(savedProduct)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(multipart("/api/products")
                        .file(productPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(productService).createProduct(any(Product.class), eq(1L), eq(2L), any());
    }

    @Test
    @DisplayName("POST /api/products - Sollte 400 Bad Request liefern wenn Validierung fehlschlägt")
    void createProduct_Invalid_ShouldReturn400() throws Exception {
        // Arrange: Invalid DTO (Name fehlt)
        ProductCreateDTO invalidDTO = new ProductCreateDTO();
        invalidDTO.setCategoryId(1L);

        String dtoJson = objectMapper.writeValueAsString(invalidDTO);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", dtoJson.getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/products")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}