package com.pretest.ecommerce.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductRequest {

    private String name;

    private String description;

    @Min(1)
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    private String category;

    private String color;

    private String imageUrl;
}
