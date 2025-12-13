package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.PagingResponse;
import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.dto.WebResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

        @Autowired
        private TransactionService transactionService;

        @Autowired
        private AuthService authService;

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<TransactionResponse>> list(
                        @RequestHeader("Authorization") String token,
                        @RequestParam(name = "status", required = false) String status,
                        @RequestParam(name = "page", defaultValue = "0") Integer page,
                        @RequestParam(name = "limit", defaultValue = "10") Integer limit) {

                User user = authService.validateToken(token);
                Page<TransactionResponse> result = transactionService.getUserTransactions(user, page, limit, status);

                return WebResponse.<List<TransactionResponse>>builder()
                                .success(true)
                                .message("Successfully retrieved transactions")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @GetMapping(path = "/{invoiceNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<TransactionResponse> get(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("invoiceNumber") String invoiceNumber) {

                User user = authService.validateToken(token);
                TransactionResponse response = transactionService.getTransactionByInvoice(user, invoiceNumber);

                return WebResponse.<TransactionResponse>builder()
                                .success(true)
                                .message("Successfully retrieved transaction detail")
                                .data(response)
                                .build();
        }

        @PostMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<TransactionResponse>> checkout(@RequestHeader("Authorization") String token) {
                User user = authService.validateToken(token);
                List<TransactionResponse> result = transactionService.checkout(user.getId());

                return WebResponse.<List<TransactionResponse>>builder()
                                .success(true)
                                .message("Checkout successful")
                                .data(result)
                                .build();
        }
}
