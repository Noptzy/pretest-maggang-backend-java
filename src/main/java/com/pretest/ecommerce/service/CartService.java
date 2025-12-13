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
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                if (!"USER".equals(user.getRole())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with role USER can shop");
                }

                Product product = productRepository.findById(request.getProductId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Product not found"));

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
                                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                "Cart not found"));
                return toResponse(cart);
        }

        @Transactional
        public CartResponse removeCartItem(UUID userId, Long cartItemId) {
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

                CartItem itemToRemove = cart.getCartItems().stream()
                                .filter(item -> item.getId().equals(cartItemId))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Product not found in cart"));

                cart.getCartItems().remove(itemToRemove);
                cartRepository.save(cart);

                return toResponse(cart);
        }

        @Transactional
        public CartResponse updateCartItem(UUID userId, Long cartItemId,
                        com.pretest.ecommerce.dto.UpdateCartRequest request) {
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

                CartItem item = cart.getCartItems().stream()
                                .filter(i -> i.getId().equals(cartItemId))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Product not found in cart"));

                if (request.getQuantity() != null) {
                        if (request.getQuantity() <= 0) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Quantity must be greater than 0");
                        }
                        // Check stock
                        if (item.getProduct().getStock() < request.getQuantity()) {
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
                        }
                        item.setQuantity(request.getQuantity());
                }

                if (request.getNote() != null) {
                        item.setNote(request.getNote());
                }

                if (request.getIsSelected() != null) {
                        item.setIsSelected(request.getIsSelected());
                }

                item.setUpdatedAt(LocalDateTime.now());
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
                                                .subtotal(item.getProduct().getPrice()
                                                                .multiply(BigDecimal.valueOf(item.getQuantity())))
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
