package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.dto.WebResponse;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthService authService;

    @PostMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<List<TransactionResponse>> checkout(
            @RequestHeader("Authorization") String token) {
        UUID userId = authService.extractUserIdFromToken(token);
        List<TransactionResponse> response = transactionService.checkout(userId);
        return WebResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Checkout successful")
                .data(response)
                .build();
    }
}
