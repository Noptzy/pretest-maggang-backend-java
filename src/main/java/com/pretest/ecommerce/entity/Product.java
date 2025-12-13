package com.pretest.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @jakarta.validation.constraints.NotBlank(message = "Product name is required")
    @jakarta.validation.constraints.Size(max = 100, message = "Product name cannot exceed 100 characters")
    private String name;

    private String description;

    @Column(nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Price is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "Price must be non-negative")
    private BigDecimal price;

    @Column(nullable = false)
    @jakarta.validation.constraints.NotNull(message = "Stock is required")
    @jakarta.validation.constraints.Min(value = 0, message = "Stock must be non-negative")
    private Integer stock;

    private String category;

    private String color;

    @Column(name = "sold_for")
    private Integer soldFor;

    private Double rating;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}
