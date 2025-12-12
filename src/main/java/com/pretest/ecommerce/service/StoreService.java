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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

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
        // createdAt and updatedAt are handled by JPA Auditing (@CreatedDate,
        // @LastModifiedDate) usually,
        // but since we are using 'new Store()', we might need to set them manually if
        // listeners aren't triggered on new entities before flush?
        // BaseEntity has @EntityListeners(AuditingEntityListener.class), so it should
        // work on save().
        // However, if we want to return them immediately in response, we might need to
        // rely on the returned instance from save().

        Store savedStore = storeRepository.save(store);

        return toResponse(savedStore);
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
        return toResponse(savedStore);
    }

    @Transactional(readOnly = true)
    public Page<StoreResponse> findAll(int page, int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        Page<Store> storesPage = storeRepository.findAll(pageable);

        List<StoreResponse> storeResponses = storesPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(storeResponses, pageable, storesPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public StoreResponse get(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return toResponse(store);
    }

    private StoreResponse toResponse(Store store) {
        boolean isOnline = authService.isUserOnline(store.getUser().getId());
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .rating(store.getRating())
                .isOnline(isOnline)
                .imageUrl(store.getImageUrl())
                .build();
    }
}