package com.pretest.ecommerce.repository;

import com.pretest.ecommerce.entity.StoreFollower;
import com.pretest.ecommerce.entity.StoreFollowerKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreFollowerRepository extends JpaRepository<StoreFollower, StoreFollowerKey> {
}
