package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.TransactionService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AuthService authService;

    @Test
    void checkoutSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        TransactionResponse response = TransactionResponse.builder()
                .id(1L)
                .invoiceNumber("INV-123")
                .totalAmount(new BigDecimal("100000"))
                .details(Collections.emptyList())
                .build();

        when(authService.extractUserIdFromToken(any())).thenReturn(userId);
        when(transactionService.checkout(userId)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(post("/api/transactions/checkout")
                .header("Authorization", "test-token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-123"));
    }
}
