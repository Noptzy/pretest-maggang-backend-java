package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.*;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.ImageService;
import com.pretest.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
public class ProductController {

        @Autowired
        private ProductService productService;

        @Autowired
        private AuthService authService;

        @Autowired
        private ImageService imageService;

        @PostMapping(path = "/api/stores/{storeId}/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> create(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("storeId") Long storeId,
                        @RequestParam("name") String name,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam("price") BigDecimal price,
                        @RequestParam("stock") Integer stock,
                        @RequestParam("category") String category,
                        @RequestParam(value = "color", required = false) String color,
                        @RequestParam(value = "imageUrl", required = false) MultipartFile imageFile) {
                User user = authService.validateToken(token);

                String imageUrl = null;
                if (imageFile != null && !imageFile.isEmpty()) {
                        imageUrl = imageService.saveImage(imageFile);
                }

                CreateProductRequest request = CreateProductRequest.builder()
                                .name(name)
                                .description(description)
                                .price(price)
                                .stock(stock)
                                .category(category)
                                .color(color)
                                .imageUrl(imageUrl)
                                .build();

                ProductResponse response = productService.create(user, storeId, request);

                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Product created successfully")
                                .data(response)
                                .build();
        }

        @GetMapping(path = "/api/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> getById(@PathVariable("productId") Long productId) {
                ProductResponse response = productService.get(productId);
                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Successfully get product")
                                .data(response)
                                .build();
        }

        @PostMapping(path = "/api/products/{productId}/rate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> rateProduct(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("productId") Long productId,
                        @RequestBody java.util.Map<String, Double> request) {

                User user = authService.validateToken(token);
                Double rating = request.get("rating");

                ProductResponse response = productService.rateProduct(user, productId, rating);

                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Product rated successfully")
                                .data(response)
                                .build();
        }
}