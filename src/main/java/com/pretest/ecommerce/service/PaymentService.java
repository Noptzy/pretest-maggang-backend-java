package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.PaymentRequest;
import com.pretest.ecommerce.dto.TransactionResponse;
import com.pretest.ecommerce.entity.Payment;
import com.pretest.ecommerce.entity.Transaction;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.PaymentRepository;
import com.pretest.ecommerce.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    public TransactionResponse pay(User user, PaymentRequest request) {
        Transaction transaction = transactionRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This transaction does not belong to you");
        }

        if (!"WAITING_PAYMENT".equals(transaction.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction is already paid or cancelled");
        }

        if (transaction.getTotalAmount().compareTo(request.getAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount does not match transaction total");
        }

        Payment payment = new Payment();
        payment.setTransaction(transaction);
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        transaction.setStatus("PAID");

        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        return transactionService.getTransactionByInvoice(user, request.getInvoiceNumber());
    }
}