package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.AddToCartRequest;
import com.pretest.ecommerce.dto.CartResponse;
import com.pretest.ecommerce.dto.WebResponse;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthService authService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<CartResponse> addToCart(
            @RequestHeader("Authorization") String token,
            @RequestBody AddToCartRequest request) {
        UUID userId = authService.extractUserIdFromToken(token);
        CartResponse response = cartService.addToCart(userId, request);
        return WebResponse.<CartResponse>builder()
                .success(true)
                .message("Product added to cart successfully")
                .data(response)
                .build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<CartResponse> getCart(
            @RequestHeader("Authorization") String token) {
        UUID userId = authService.extractUserIdFromToken(token);
        CartResponse response = cartService.getCart(userId);
        return WebResponse.<CartResponse>builder()
                .success(true)
                .message("Successfully get cart")
                .data(response)
                .build();
    }

    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<CartResponse> deleteCartItem(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long cartItemId) {
        UUID userId = authService.extractUserIdFromToken(token);
        CartResponse response = cartService.removeCartItem(userId, cartItemId);
        return WebResponse.<CartResponse>builder()
                .success(true)
                .message("Successfully delete product from cart")
                .data(response)
                .build();
    }
}
