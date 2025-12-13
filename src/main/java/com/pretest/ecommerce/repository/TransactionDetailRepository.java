package com.pretest.ecommerce.repository;

import com.pretest.ecommerce.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    Optional<TransactionDetail> findFirstByTransaction_User_IdAndProduct_IdAndIsReviewedFalse(UUID userId, Long productId);
}
