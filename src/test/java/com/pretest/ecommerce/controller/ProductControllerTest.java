package com.pretest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.CreateProductRequest;
import com.pretest.ecommerce.dto.ProductResponse;
import com.pretest.ecommerce.dto.SearchProductRequest;
import com.pretest.ecommerce.dto.UpdateProductRequest;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.ImageService;
import com.pretest.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                when(productService.create(any(User.class), any(CreateProductRequest.class))).thenReturn(response);

                mockMvc.perform(multipart("/api/products")
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
        void searchProductSuccess() throws Exception {
                ProductResponse response = ProductResponse.builder()
                                .id(1L)
                                .name("Test Product")
                                .build();
                Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(response));

                when(productService.search(any(SearchProductRequest.class))).thenReturn(page);

                mockMvc.perform(get("/api/products")
                                .param("name", "Test")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
        }

        @Test
        void deleteProductSuccess() throws Exception {
                Long productId = 1L;

                when(authService.validateToken(any())).thenReturn(user);

                mockMvc.perform(delete("/api/products/{productId}", productId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
        }

        @Test
        void updateProductSuccess() throws Exception {
                Long productId = 1L;
                MockMultipartFile imageFile = new MockMultipartFile("imageUrl", "test.jpg", "image/jpeg",
                                "test image content".getBytes());
                String imageUrl = "path/to/test.jpg";

                ProductResponse response = ProductResponse.builder()
                                .id(productId)
                                .name("Updated Product")
                                .price(new BigDecimal("20000"))
                                .imageUrl(imageUrl)
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(imageService.saveImage(any())).thenReturn(imageUrl);
                when(productService.update(any(User.class), eq(productId), any(UpdateProductRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(multipart(HttpMethod.PUT, "/api/products/{productId}", productId)
                                .file(imageFile)
                                .header("Authorization", "test-token")
                                .param("name", "Updated Product")
                                .param("price", "20000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                                .andExpect(jsonPath("$.data.name").value("Updated Product"))
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
}