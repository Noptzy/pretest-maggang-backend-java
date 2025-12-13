package com.pretest.ecommerce.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCartRequest {
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String note;

    private Boolean isSelected;
}
