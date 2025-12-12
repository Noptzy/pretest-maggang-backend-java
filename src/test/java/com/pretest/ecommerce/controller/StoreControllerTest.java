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
import static org.mockito.Mockito.when;
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

                mockMvc.perform(post("/api/stores")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
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
                                .andExpect(jsonPath("$.data[0].name").value("Test Store"));
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
}