package com.pretest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.PaymentRequest;
import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void paySuccess() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .invoiceNumber("INV-123")
                .amount(new BigDecimal("10000"))
                .method("CREDIT_CARD")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .invoiceNumber("INV-123")
                .status("PAID")
                .build();

        User user = new User(); // Mock user

        when(authService.validateToken(any())).thenReturn(user);
        when(paymentService.pay(any(User.class), any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-123"))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void payFailed() throws Exception {
        PaymentRequest request = PaymentRequest.builder()
                .invoiceNumber("INV-123")
                .amount(new BigDecimal("10000"))
                .method("CREDIT_CARD")
                .build();

        when(authService.validateToken(any())).thenReturn(new User());
        when(paymentService.pay(any(User.class), any(PaymentRequest.class)))
                .thenThrow(
                        new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Payment failed"));

        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payment failed"));
    }
}
