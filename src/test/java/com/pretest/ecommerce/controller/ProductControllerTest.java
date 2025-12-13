package com.pretest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.CreateProductRequest;
import com.pretest.ecommerce.dto.ProductResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.ImageService;
import com.pretest.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ProductService productService;

        @MockBean
        private AuthService authService;

        @MockBean
        private ImageService imageService;

        @Autowired
        private ObjectMapper objectMapper;

        private User user;

        @BeforeEach
        void setUp() {
                user = new User();
                user.setId(UUID.randomUUID());
                user.setEmail("test@gmail.com");
                user.setName("Test User");
        }

        @Test
        void createProductSuccess() throws Exception {
                MockMultipartFile imageFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg",
                                "test image content".getBytes());
                String imageUrl = "path/to/test.jpg";

                ProductResponse response = ProductResponse.builder()
                                .id(1L)
                                .name("Test Product")
                                .description("Description")
                                .price(new BigDecimal("10000"))
                                .stock(10)
                                .category("Electronics")
                                .imageUrl(imageUrl)
                                .storeName("Test Store")
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(imageService.saveImage(any())).thenReturn(imageUrl);
                when(productService.create(any(User.class), org.mockito.ArgumentMatchers.anyLong(),
                                any(CreateProductRequest.class))).thenReturn(response);

                mockMvc.perform(multipart("/api/stores/1/products")
                                .file(imageFile)
                                .header("Authorization", "test-token")
                                .param("name", "Test Product")
                                .param("description", "Description")
                                .param("price", "10000")
                                .param("stock", "10")
                                .param("category", "Electronics"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Test Product"))
                                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl));
        }

        @Test
        void getProductByIdSuccess() throws Exception {
                Long productId = 1L;
                ProductResponse response = ProductResponse.builder()
                                .id(productId)
                                .name("Test Product")
                                .storeName("Test Store")
                                .build();

                when(productService.get(productId)).thenReturn(response);

                mockMvc.perform(get("/api/products/{productId}", productId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Successfully get product"))
                                .andExpect(jsonPath("$.data.name").value("Test Product"));
        }

        @Test
        void rateProductSuccess() throws Exception {
                Long productId = 1L;
                ProductResponse response = ProductResponse.builder()
                                .id(productId)
                                .rating(4.5)
                                .build();

                java.util.Map<String, Double> ratingRequest = java.util.Collections.singletonMap("rating", 4.5);

                when(authService.validateToken(any())).thenReturn(user);
                when(productService.rateProduct(any(User.class), eq(productId), eq(4.5))).thenReturn(response);

                mockMvc.perform(post("/api/products/{productId}/rate", productId)
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ratingRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Product rated successfully"))
                                .andExpect(jsonPath("$.data.rating").value(4.5));
        }

        @Test
        void rateProductFailed() throws Exception {
                Long productId = 1L;
                java.util.Map<String, Double> ratingRequest = java.util.Collections.singletonMap("rating", 6.0); // Invalid
                                                                                                                 // Rating

                when(authService.validateToken(any())).thenReturn(user);
                when(productService.rateProduct(any(User.class), eq(productId), eq(6.0)))
                                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid rating"));

                mockMvc.perform(post("/api/products/{productId}/rate", productId)
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ratingRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Invalid rating"));
        }
}