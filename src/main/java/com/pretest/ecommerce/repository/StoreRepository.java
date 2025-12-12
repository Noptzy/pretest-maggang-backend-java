package com.pretest.ecommerce.repository;

import com.pretest.ecommerce.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByUserId(UUID userId);
    Optional<Store> findByUserId(UUID userId);
}
