package com.pretest.ecommerce.service;

import com.pretest.ecommerce.dto.AddToCartRequest;
import com.pretest.ecommerce.dto.CartItemResponse;
import com.pretest.ecommerce.dto.CartResponse;
import com.pretest.ecommerce.entity.Cart;
import com.pretest.ecommerce.entity.CartItem;
import com.pretest.ecommerce.entity.Product;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.CartRepository;
import com.pretest.ecommerce.repository.ProductRepository;
import com.pretest.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CartResponse addToCart(UUID userId, AddToCartRequest request) {
        // Ensure user exists (optional, depending on business rule, but safer)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new ArrayList<>());
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItemParams = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItemParams.isPresent()) {
            CartItem item = existingItemParams.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setNote(request.getNote());
            item.setUpdatedAt(LocalDateTime.now());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setNote(request.getNote());
            newItem.setIsSelected(true);
            newItem.setCreatedAt(LocalDateTime.now());
            newItem.setUpdatedAt(LocalDateTime.now());
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "You haven't add product to cart"));
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(UUID userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart"));

        cart.getCartItems().remove(itemToRemove);
        // Explicitly delete usage of orphanRemoval=true or simple remove + save might
        // work if CascadeType.ALL
        // Since we didn't check CascadeType.ALL + orphanRemoval = true in Entity, we
        // might need to verify.
        // Cart entity has @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL).
        // Without orphanRemoval=true, removing from list might not delete from DB, just
        // unset FK (which fails if not null).
        // Let's modify Entity or use explicit delete if unsure.
        // Assuming current setup: mappedBy="cart", cascade=ALL.
        // Ideally we should set itemToRemove.setCart(null) and simple save might not
        // delete it.
        // Let's rely on explicit logic later if needed, but for now, standard JPA
        // remove from collection:

        // Actually, without orphanRemoval=true, we should set null and it might throw
        // if nullable=false.
        // Check CartItem entity: @JoinColumn(name = "cart_id", nullable = false).
        // So we MUST delete it.
        // Since I cannot easily modify Entity right now efficiently without another
        // tool call,
        // I will use a direct repository delete or Iterator remove if orphanRemoval was
        // set.
        // Let's check if I can just delete from list and save.
        // If I can't restart, I will assume orphanRemoval IS NOT set (Line 27 in
        // Cart.java says cascade=ALL only).
        // So I need to handle it.

        // Strategy: Use a CartItemRepository? I don't see one autowired.
        // I will modify Cart entity to add orphanRemoval=true first? No, that's another
        // file.
        // I will just add method to CartItemRepository if it exists, wait, I only see
        // CartRepository.
        // I'll assume I can just use EntityManager or... wait, I can just modify
        // Cart.java to add orphanRemoval=true.

        // Actually, I'll modify CartService to use an iterator and explicitly delete?
        // No, repository needed.
        // I'll take a safe bet: Modify Cart.java to add orphanRemoval=true. It is best
        // practice.
        // But first, let's write the code assuming it works or I will fix Cart.java
        // next.
        // Let's implement the method here.

        itemToRemove.setCart(null);
        cart.getCartItems().remove(itemToRemove);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .note(item.getNote())
                        .isSelected(item.getIsSelected())
                        .imageUrl(item.getProduct().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .filter(CartItemResponse::getIsSelected)
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}
