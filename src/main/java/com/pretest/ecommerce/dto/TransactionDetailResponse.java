package com.pretest.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productVariant;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private String note;
    private String imageUrl;
}
