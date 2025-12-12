package com.pretest.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateStoreRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String location;

    private String imageUrl;
}