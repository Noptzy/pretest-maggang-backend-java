package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.entity.User;
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
import static org.mockito.ArgumentMatchers.eq;
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

                User user = new User();
                user.setId(userId);
                when(authService.validateToken(any())).thenReturn(user);
                when(transactionService.checkout(userId)).thenReturn(Collections.singletonList(response));

                mockMvc.perform(post("/api/transactions/checkout")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-123"));
        }

        @Test
        void checkoutFailed() throws Exception {
                UUID userId = UUID.randomUUID();
                User user = new User();
                user.setId(userId);

                when(authService.validateToken(any())).thenReturn(user);
                when(transactionService.checkout(userId))
                                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.BAD_REQUEST, "Cart is empty"));

                mockMvc.perform(post("/api/transactions/checkout")
                                .header("Authorization", "test-token")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Cart is empty"));
        }

        @Test
        void listTransactionsSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                User user = new User();
                user.setId(userId);

                TransactionResponse response = TransactionResponse.builder()
                                .id(1L)
                                .invoiceNumber("INV-123")
                                .build();

                org.springframework.data.domain.Page<TransactionResponse> page = new org.springframework.data.domain.PageImpl<>(
                                Collections.singletonList(response));

                when(authService.validateToken(any())).thenReturn(user);
                when(transactionService.getUserTransactions(any(User.class), any(Integer.class), any(Integer.class),
                                any()))
                                .thenReturn(page);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/transactions")
                                .header("Authorization", "test-token")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-123"));
        }

        @Test
        void getTransactionByInvoiceSuccess() throws Exception {
                UUID userId = UUID.randomUUID();
                User user = new User();
                user.setId(userId);
                String invoiceNumber = "INV-123";

                TransactionResponse response = TransactionResponse.builder()
                                .id(1L)
                                .invoiceNumber(invoiceNumber)
                                .build();

                when(authService.validateToken(any())).thenReturn(user);
                when(transactionService.getTransactionByInvoice(any(User.class), eq(invoiceNumber)))
                                .thenReturn(response);

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/transactions/{invoiceNumber}", invoiceNumber)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.invoiceNumber").value(invoiceNumber));
        }

        @Test
        void getTransactionByInvoiceFailed() throws Exception {
                UUID userId = UUID.randomUUID();
                User user = new User();
                user.setId(userId);
                String invoiceNumber = "INV-INVALID";

                when(authService.validateToken(any())).thenReturn(user);
                when(transactionService.getTransactionByInvoice(any(User.class), eq(invoiceNumber)))
                                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                                                org.springframework.http.HttpStatus.NOT_FOUND,
                                                "Transaction not found"));

                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/transactions/{invoiceNumber}", invoiceNumber)
                                .header("Authorization", "test-token"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value("Transaction not found"));
        }

}
