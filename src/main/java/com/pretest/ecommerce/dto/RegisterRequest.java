package com.pretest.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    @Email(message = "Email is required")
    private String email;

    @NotBlank
    @Size(min=2, max=100, message = "name character minimum length is 2")
    private String name;

    @NotBlank
    @Size(min=8, max=30, message = "Password character minimum length is 8")
    private String password;
}