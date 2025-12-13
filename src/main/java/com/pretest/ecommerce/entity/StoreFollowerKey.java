package com.pretest.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreFollowerKey implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "store_id")
    private Long storeId;
}
