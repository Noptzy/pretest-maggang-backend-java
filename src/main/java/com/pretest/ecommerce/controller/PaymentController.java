package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.PaymentRequest;
import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.dto.WebResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthService authService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TransactionResponse> pay(
            @RequestHeader("Authorization") String token,
            @RequestBody PaymentRequest request) {

        User user = authService.validateToken(token);
        TransactionResponse response = paymentService.pay(user, request);

        return WebResponse.<TransactionResponse>builder()
                .success(true)
                .message("Payment successful")
                .data(response)
                .build();
    }
}