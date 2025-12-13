package com.pretest.ecommerce.repository;

import com.pretest.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @org.springframework.data.jpa.repository.Query("SELECT AVG(p.rating) FROM Product p WHERE p.store.id = :storeId")
    Double getAverageRatingByStoreId(Long storeId);
}
