package com.pretest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.CreateStoreRequest;
import com.pretest.ecommerce.dto.StoreResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@WebMvcTest(StoreController.class)
public class StoreControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private StoreService storeService;

        @MockBean
        private AuthService authService;

        @Autowired
        private ObjectMapper objectMapper;

        private User user;

        @BeforeEach
        void setUp() {
                user = new User();
                user.setId(UUID.randomUUID());
                user.setEmail("test@gmail.com");
                user.setName("Test User");
                user.setRole("SELLER");
        }

        @Test
        void createStoreSuccess() throws Exception {
                CreateStoreRequest request = new CreateStoreRequest();
                request.setName("Test Store");
                request.setLocation("Test Location");

                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name(request.getName())
                                .location(request.getLocation())
                                .isOnline(true)
                                .rating(BigDecimal.ZERO)
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.create(any(User.class), any(CreateStoreRequest.class))).thenReturn(storeResponse);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/stores")
                                .param("name", "Test Store")
                                .param("location", "Test Location")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Store created successfully"))
                                .andExpect(jsonPath("$.data.name").value("Test Store"));
        }

        @Test
        void createStoreSuccessJson() throws Exception {
                CreateStoreRequest request = new CreateStoreRequest();
                request.setName("Test Store");
                request.setLocation("Test Location");

                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name(request.getName())
                                .location(request.getLocation())
                                .isOnline(true)
                                .rating(BigDecimal.ZERO)
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.create(any(User.class), any(CreateStoreRequest.class))).thenReturn(storeResponse);

                mockMvc.perform(post("/api/stores")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Store created successfully"))
                                .andExpect(jsonPath("$.data.name").value("Test Store"));
        }

        @Test
        void createStoreFailed_alreadyExists() throws Exception {
                CreateStoreRequest request = new CreateStoreRequest();
                request.setName("Test Store");
                request.setLocation("Test Location");

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.create(any(User.class), any(CreateStoreRequest.class)))
                                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                                                "User already has a store"));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/stores")
                                .param("name", "Test Store")
                                .param("location", "Test Location")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void createStoreFailed_wrongRole() throws Exception {
                // User with ROLE_USER (not SELLER)
                User normalUser = new User();
                normalUser.setId(UUID.randomUUID());
                normalUser.setEmail("user@gmail.com");
                normalUser.setName("Normal User");
                normalUser.setRole("USER");

                when(authService.validateToken(any())).thenReturn(normalUser);
                when(storeService.create(any(User.class), any(CreateStoreRequest.class)))
                                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                                                "Only Seller can create a store"));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/stores")
                                .param("name", "Test Store")
                                .param("location", "Test Location")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Only Seller can create a store"));
        }

        @Test
        void findAllSuccess() throws Exception {
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name("Test Store")
                                .location("Test Location")
                                .isOnline(true)
                                .rating(BigDecimal.ZERO)
                                .build();

                Page<StoreResponse> page = new PageImpl<>(Collections.singletonList(storeResponse));

                when(storeService.findAll(0, 10)).thenReturn(page);

                mockMvc.perform(get("/api/stores")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].name").value("Test Store"))
                                .andExpect(jsonPath("$.data[0].products").doesNotExist());
        }

        @MockBean
        private org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration errorMvcAutoConfiguration;

        // Using simple mock as ProductService is not field of StoreControllerTest yet
        // so I need to add it to field or just rely on MockBean.
        // Wait, I see ProductService injected in StoreController but I need to mock it
        // in test.

        // Let's add ProductService specific tests

        @MockBean
        private com.pretest.ecommerce.service.ProductService productService;

        @Test
        void getProductSuccess() throws Exception {
                Long storeId = 1L;
                Long productId = 1L;
                com.pretest.ecommerce.dto.ProductResponse response = com.pretest.ecommerce.dto.ProductResponse.builder()
                                .id(productId)
                                .name("Test Product")
                                .storeName("Test Store")
                                .storeId(storeId)
                                .build();

                when(productService.get(productId)).thenReturn(response);

                mockMvc.perform(get("/api/stores/{storeId}/products/{productId}", storeId, productId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Test Product"));
        }

        @Test
        void getStoreProductsSuccess() throws Exception {
                Long storeId = 1L;
                com.pretest.ecommerce.dto.ProductResponse response = com.pretest.ecommerce.dto.ProductResponse.builder()
                                .id(1L)
                                .name("Test Product")
                                .storeName("Test Store")
                                .build();
                Page<com.pretest.ecommerce.dto.ProductResponse> page = new PageImpl<>(
                                Collections.singletonList(response));

                when(productService.search(any(com.pretest.ecommerce.dto.SearchProductRequest.class))).thenReturn(page);

                mockMvc.perform(get("/api/stores/{storeId}/products", storeId)
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
        }

        @Test
        void getStoreByIdSuccess() throws Exception {
                Long storeId = 1L;
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(storeId)
                                .name("Test Store")
                                .products(Collections.singletonList(com.pretest.ecommerce.dto.ProductResponse.builder()
                                                .id(1L)
                                                .name("Test Product")
                                                .build()))
                                .build();

                when(storeService.get(storeId)).thenReturn(storeResponse);

                mockMvc.perform(get("/api/stores/{storeId}", storeId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Test Store"))
                                .andExpect(jsonPath("$.data.products[0].name").value("Test Product"));
        }

        @MockBean
        private com.pretest.ecommerce.service.ImageService imageService;

        @Test
        void getMyStoreSuccess() throws Exception {
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name("My Store")
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.findByUser(any(User.class))).thenReturn(storeResponse);

                mockMvc.perform(get("/api/stores/my-store")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("My Store"));
        }

        @Test
        void updateStoreSuccess() throws Exception {
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name("Updated Store")
                                .location("Updated Location")
                                .imageUrl("http://example.com/image.jpg")
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(imageService.saveImage(any())).thenReturn("http://example.com/image.jpg");
                when(storeService.update(any(User.class), any(com.pretest.ecommerce.dto.UpdateStoreRequest.class)))
                                .thenReturn(storeResponse);

                org.springframework.mock.web.MockMultipartFile image = new org.springframework.mock.web.MockMultipartFile(
                                "imageUrl", "image.jpg", "image/jpeg", "test image".getBytes());

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart(org.springframework.http.HttpMethod.POST, "/api/stores/my-store")
                                .file(image)
                                .param("name", "Updated Store")
                                .param("location", "Updated Location")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Updated Store"))
                                .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/image.jpg"));
        }

        @Test
        void updateStoreSuccessJson() throws Exception {
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name("Updated Store")
                                .location("Updated Location")
                                .imageUrl("http://example.com/image.jpg")
                                .build();

                com.pretest.ecommerce.dto.UpdateStoreRequest request = com.pretest.ecommerce.dto.UpdateStoreRequest
                                .builder()
                                .name("Updated Store")
                                .location("Updated Location")
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.update(any(User.class), any(com.pretest.ecommerce.dto.UpdateStoreRequest.class)))
                                .thenReturn(storeResponse);

                mockMvc.perform(post("/api/stores/my-store")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Updated Store"));
        }

        @Test
        void listAllProductsSuccess() throws Exception {
                com.pretest.ecommerce.dto.ProductResponse response = com.pretest.ecommerce.dto.ProductResponse.builder()
                                .id(1L)
                                .name("Test Product")
                                .build();
                Page<com.pretest.ecommerce.dto.ProductResponse> page = new PageImpl<>(
                                Collections.singletonList(response));

                when(productService.search(any(com.pretest.ecommerce.dto.SearchProductRequest.class))).thenReturn(page);

                mockMvc.perform(get("/api/stores/products")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].name").value("Test Product"));
        }

        @Test
        void updateProductMyStoreSuccess() throws Exception {
                Long productId = 1L;
                com.pretest.ecommerce.dto.ProductResponse response = com.pretest.ecommerce.dto.ProductResponse.builder()
                                .id(productId)
                                .name("Updated Product")
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(productService.update(any(User.class), any(Long.class),
                                any(com.pretest.ecommerce.dto.UpdateProductRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart(
                                org.springframework.http.HttpMethod.PUT, "/api/stores/my-store/products/{productId}",
                                productId)
                                .param("name", "Updated Product")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Updated Product"));
        }

        @Test
        void deleteProductMyStoreSuccess() throws Exception {
                Long productId = 1L;

                when(authService.validateToken(any())).thenReturn(user);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                "/api/stores/my-store/products/{productId}", productId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
        }

        @Test
        void updateProductMyStoreFailed_notOwner() throws Exception {
                Long productId = 1L;
                when(authService.validateToken(any())).thenReturn(user);
                when(productService.update(any(User.class), eq(productId),
                                any(com.pretest.ecommerce.dto.UpdateProductRequest.class)))
                                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                                                "Product does not belong to your store"));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart(
                                org.springframework.http.HttpMethod.PUT, "/api/stores/my-store/products/{productId}",
                                productId)
                                .param("name", "Updated Product")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Product does not belong to your store"));
        }

        @Test
        void deleteProductMyStoreFailed_notOwner() throws Exception {
                Long productId = 1L;
                when(authService.validateToken(any())).thenReturn(user);
                doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                                "Product does not belong to your store"))
                                .when(productService).delete(any(User.class), eq(productId));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                "/api/stores/my-store/products/{productId}", productId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Product does not belong to your store"));
        }

        @Test
        void getMyStoreProductsSuccess() throws Exception {
                StoreResponse storeResponse = StoreResponse.builder()
                                .id(1L)
                                .name("My Store")
                                .build();

                com.pretest.ecommerce.dto.ProductResponse response = com.pretest.ecommerce.dto.ProductResponse.builder()
                                .id(1L)
                                .name("My Product")
                                .build();
                Page<com.pretest.ecommerce.dto.ProductResponse> page = new PageImpl<>(
                                Collections.singletonList(response));

                when(authService.validateToken(any())).thenReturn(user);
                when(storeService.findByUser(any(User.class))).thenReturn(storeResponse);
                when(productService.search(any(com.pretest.ecommerce.dto.SearchProductRequest.class))).thenReturn(page);

                mockMvc.perform(get("/api/stores/my-store/products")
                                .header("Authorization", "test-token")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Successfully get my store products"))
                                .andExpect(jsonPath("$.data[0].name").value("My Product"));
        }

        @Test
        void createProductMyStoreSuccess() throws Exception {
                String token = "Bearer valid_token";
                User user = new User();
                user.setId(java.util.UUID.randomUUID());

                com.pretest.ecommerce.dto.CreateProductRequest request = new com.pretest.ecommerce.dto.CreateProductRequest();
                request.setName("Test Product");
                request.setPrice(java.math.BigDecimal.valueOf(100));
                request.setStock(10);
                request.setCategory("Test Category");

                com.pretest.ecommerce.dto.StoreResponse storeResponse = com.pretest.ecommerce.dto.StoreResponse
                                .builder()
                                .id(1L)
                                .name("My Store")
                                .build();

                com.pretest.ecommerce.dto.ProductResponse productResponse = com.pretest.ecommerce.dto.ProductResponse
                                .builder()
                                .id(1L)
                                .name("Test Product")
                                .build();

                org.springframework.mock.web.MockMultipartFile imageFile = new org.springframework.mock.web.MockMultipartFile(
                                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

                when(authService.validateToken(token)).thenReturn(user);
                when(storeService.findByUser(user)).thenReturn(storeResponse);
                when(productService.create(any(User.class), any(Long.class),
                                any(com.pretest.ecommerce.dto.CreateProductRequest.class))).thenReturn(productResponse);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .multipart("/api/stores/my-store/products")
                                .file(imageFile)
                                .param("name", request.getName())
                                .param("price", request.getPrice().toString())
                                .param("stock", request.getStock().toString())
                                .param("category", request.getCategory())
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Product created successfully"))
                                .andExpect(jsonPath("$.data.name").value("Test Product"));
        }

        @Test
        void deleteMyStoreSuccess() throws Exception {
                String token = "Bearer valid_token";
                User user = new User();
                user.setId(java.util.UUID.randomUUID());

                when(authService.validateToken(token)).thenReturn(user);
                doNothing().when(storeService).delete(user);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/stores/my-store")
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Store deleted successfully"));

                verify(storeService, org.mockito.Mockito.times(1)).delete(user);
        }
}