package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.CreateProductRequest;
import com.pretest.ecommerce.dto.ProductResponse;
import com.pretest.ecommerce.dto.SearchProductRequest;
import com.pretest.ecommerce.dto.UpdateProductRequest;
import com.pretest.ecommerce.entity.Product;
import com.pretest.ecommerce.entity.Store;
import com.pretest.ecommerce.entity.TransactionDetail;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.ProductRepository;
import com.pretest.ecommerce.repository.StoreRepository;
import com.pretest.ecommerce.repository.TransactionDetailRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Transactional
    public ProductResponse create(User user, Long storeId, CreateProductRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (!store.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this store");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setColor(request.getColor());
        product.setImageUrl(request.getImageUrl());
        product.setStore(store);
        product.setSoldFor(0);
        product.setRating(0.0);

        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(User user, Long productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Store userStore = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have a store"));

        if (!product.getStore().getId().equals(userStore.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Product does not belong to your store");
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getColor() != null) product.setColor(request.getColor());

        productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void delete(User user, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Store userStore = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have a store"));

        if (!product.getStore().getId().equals(userStore.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Product does not belong to your store");
        }

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(SearchProductRequest request) {
        Specification<Product> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(request.getName())) {
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
            }
            if (Objects.nonNull(request.getCategory())) {
                predicates.add(builder.equal(root.get("category"), request.getCategory()));
            }
            if (Objects.nonNull(request.getMinPrice())) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            if (Objects.nonNull(request.getMaxPrice())) {
                predicates.add(builder.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }
            if (Objects.nonNull(request.getStoreId())) {
                predicates.add(builder.equal(root.get("store").get("id"), request.getStoreId()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getLimit());
        Page<Product> products = productRepository.findAll(specification, pageable);

        List<ProductResponse> responses = products.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, products.getTotalElements());
    }

    @Transactional
    public ProductResponse rateProduct(User user, Long productId, Double rating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 1. Validate Seller cannot rate own product
        if (product.getStore().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller cannot rate their own product");
        }

        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        // 2. NEW LOGIC: Check Transaction Detail directly (Must be bought AND not reviewed)
        TransactionDetail detail = transactionDetailRepository
                .findFirstByTransaction_User_IdAndProduct_IdAndIsReviewedFalse(user.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "You have already rated all your purchases for this product or haven't bought it yet"));

        // 3. Mark as reviewed so it can't be used again
        detail.setIsReviewed(true);
        transactionDetailRepository.save(detail);

        // 4. Calculate New Average Rating
        Double currentRating = product.getRating() == null ? 0.0 : product.getRating();
        int n = product.getSoldFor() == null ? 0 : product.getSoldFor();

        // Weighted Average Formula
        Double newRating = ((currentRating * n) + rating) / (n + 1);

        product.setRating(newRating);
        productRepository.save(product);

        // 5. Update Store Average Rating
        Double avgStoreRating = productRepository.getAverageRatingByStoreId(product.getStore().getId());
        if (avgStoreRating != null) {
            Store store = product.getStore();
            store.setRating(BigDecimal.valueOf(avgStoreRating));
            storeRepository.save(store);
        }

        return toResponse(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
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
                .storeId(product.getStore().getId())
                .storeName(product.getStore().getName())
                .storeLocation(product.getStore().getLocation())
                .storeRating(product.getStore().getRating())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}