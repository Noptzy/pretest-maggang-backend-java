package com.pretest.ecommerce.repository;

import com.pretest.ecommerce.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByInvoiceNumber(String invoiceNumber);

    boolean existsByUserIdAndTransactionDetails_ProductId(UUID userId, Long productId);
}