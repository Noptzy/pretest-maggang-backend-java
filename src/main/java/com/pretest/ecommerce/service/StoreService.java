package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.CreateStoreRequest;
import com.pretest.ecommerce.dto.StoreResponse;
import com.pretest.ecommerce.entity.Store;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import com.pretest.ecommerce.entity.StoreFollower;
import com.pretest.ecommerce.entity.StoreFollowerKey;
import com.pretest.ecommerce.repository.StoreFollowerRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreFollowerRepository storeFollowerRepository;

    @Autowired
    private com.pretest.ecommerce.service.AuthService authService;

    @Transactional
    public StoreResponse create(User user, CreateStoreRequest request) {

        if (!"SELLER".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Seller can create a store");
        }

        if (storeRepository.existsByUserId(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has a store");
        }

        Store store = new Store();
        store.setName(request.getName());
        store.setLocation(request.getLocation());
        store.setUser(user);
        store.setIsOnline(true);
        store.setRating(BigDecimal.ZERO);
        store.setImageUrl(request.getImageUrl());
        Store savedStore = storeRepository.save(store);

        return toResponse(savedStore, true);
    }

    @Transactional
    public StoreResponse update(User user, com.pretest.ecommerce.dto.UpdateStoreRequest request) {
        Store store = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (request.getName() != null) {
            store.setName(request.getName());
        }
        if (request.getLocation() != null) {
            store.setLocation(request.getLocation());
        }
        if (request.getImageUrl() != null) {
            store.setImageUrl(request.getImageUrl());
        }

        Store savedStore = storeRepository.save(store);
        return toResponse(savedStore, false);
    }

    @Transactional(readOnly = true)
    public Page<StoreResponse> findAll(int page, int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        Page<Store> storesPage = storeRepository.findAll(pageable);

        List<StoreResponse> storeResponses = storesPage.getContent().stream()
                .map(store -> toResponse(store, false))
                .collect(Collectors.toList());

        return new PageImpl<>(storeResponses, pageable, storesPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public StoreResponse get(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return toResponse(store, true);
    }

    @Transactional(readOnly = true)
    public StoreResponse findByUser(User user) {
        Store store = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return toResponse(store, true);
    }

    private StoreResponse toResponse(Store store, boolean includeProducts) {
        boolean isOnline = authService.isUserOnline(store.getUser().getId());

        List<com.pretest.ecommerce.dto.ProductResponse> productResponses = null;
        if (includeProducts && store.getProducts() != null) {
            productResponses = store.getProducts().stream()
                    .map(product -> com.pretest.ecommerce.dto.ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .stock(product.getStock())
                            .category(product.getCategory())
                            .color(product.getColor())
                            .imageUrl(product.getImageUrl())
                            .soldFor(product.getSoldFor())
                            .rating(product.getRating())
                            .storeId(store.getId())
                            .storeName(store.getName())
                            .storeLocation(store.getLocation())
                            .storeRating(store.getRating())
                            .build())
                    .collect(Collectors.toList());
        }

        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .rating(store.getRating())
                .isOnline(isOnline)
                .imageUrl(store.getImageUrl())
                .products(productResponses)
                .build();
    }

    @Transactional
    public void followStore(User user, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (store.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "you can't follow your own store :3");
        }

        StoreFollowerKey key = new StoreFollowerKey(user.getId(), storeId);
        if (storeFollowerRepository.existsById(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already following this store");
        }

        StoreFollower follower = new StoreFollower();
        follower.setId(key);
        follower.setUser(user);
        follower.setStore(store);
        follower.setFollowedAt(LocalDateTime.now());

        storeFollowerRepository.save(follower);
    }

    @Transactional
    public void unfollowStore(User user, Long storeId) {
        StoreFollowerKey key = new StoreFollowerKey(user.getId(), storeId);
        StoreFollower follower = storeFollowerRepository.findById(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not following this store"));

        storeFollowerRepository.delete(follower);
    }

    @Transactional
    public void delete(User user) {
        Store store = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        storeRepository.delete(store);
    }
}