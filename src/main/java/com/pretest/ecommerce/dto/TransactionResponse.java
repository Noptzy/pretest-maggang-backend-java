package com.pretest.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String userId;
    private Long storeId;
    private String storeName;
    private String invoiceNumber;
    private BigDecimal totalAmount;
    private BigDecimal shippingCost;
    private String status;
    private LocalDateTime createdAt;
    private List<TransactionDetailResponse> details;
}