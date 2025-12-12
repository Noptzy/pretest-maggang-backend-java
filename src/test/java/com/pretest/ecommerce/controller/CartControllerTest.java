package com.pretest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.AddToCartRequest;
import com.pretest.ecommerce.dto.CartResponse;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CartService cartService;

        @MockBean
        private AuthService authService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void addToCartSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                AddToCartRequest request = new AddToCartRequest(1L, 2, "Note");
                CartResponse response = CartResponse.builder()
                                .id(1L)
                                .items(Collections.emptyList())
                                .totalAmount(BigDecimal.ZERO)
                                .build();

                when(authService.extractUserIdFromToken(any())).thenReturn(userId);
                when(cartService.addToCart(eq(userId), any(AddToCartRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/carts")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void getCartSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                CartResponse response = CartResponse.builder()
                                .id(1L)
                                .items(Collections.emptyList())
                                .totalAmount(BigDecimal.ZERO)
                                .build();

                when(authService.extractUserIdFromToken(any())).thenReturn(userId);
                when(cartService.getCart(userId)).thenReturn(response);

                mockMvc.perform(get("/api/carts")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void getCartFailed() throws Exception {
                UUID userId = UUID.randomUUID();

                when(authService.extractUserIdFromToken(any())).thenReturn(userId);
                when(cartService.getCart(userId)).thenThrow(new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "You haven't add product to cart"));

                mockMvc.perform(get("/api/carts")
                                .header("Authorization", "test-token"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("You haven't add product to cart"));
        }

        @Test
        void deleteCartItemSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                Long cartItemId = 1L;
                CartResponse response = CartResponse.builder()
                                .id(1L)
                                .items(Collections.emptyList())
                                .totalAmount(BigDecimal.ZERO)
                                .build();

                when(authService.extractUserIdFromToken(any())).thenReturn(userId);
                when(cartService.removeCartItem(userId, cartItemId)).thenReturn(response);

                mockMvc.perform(delete("/api/carts/{id}", cartItemId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Successfully delete product from cart"));
        }

        @Test
        void deleteCartItemFailed() throws Exception {
                UUID userId = UUID.randomUUID();
                Long cartItemId = 1L;

                when(authService.extractUserIdFromToken(any())).thenReturn(userId);
                when(cartService.removeCartItem(userId, cartItemId))
                                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.NOT_FOUND,
                                                "Product not found in cart"));

                mockMvc.perform(delete("/api/carts/{id}", cartItemId)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Product not found in cart"));
        }
}
