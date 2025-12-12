package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.CreateProductRequest;
import com.pretest.ecommerce.dto.ProductResponse;
import com.pretest.ecommerce.dto.SearchProductRequest;
import com.pretest.ecommerce.entity.Product;
import com.pretest.ecommerce.entity.Store;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.ProductRepository;
import com.pretest.ecommerce.repository.StoreRepository;
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

    @Transactional
    public ProductResponse create(User user, CreateProductRequest request) {

        Store store = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You must create a store first"));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
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
    public ProductResponse update(User user, Long productId,
            com.pretest.ecommerce.dto.UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Store userStore = storeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have a store"));

        // Verify that the product belongs to the store where user is owner
        if (!product.getStore().getId().equals(userStore.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Product does not belong to your store");
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }

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
                predicates.add(
                        builder.like(builder.lower(root.get("name")), "%" + request.getName().toLowerCase() + "%"));
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
                .build();
    }
}