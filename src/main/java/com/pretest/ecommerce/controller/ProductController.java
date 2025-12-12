package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.*;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.ImageService;
import com.pretest.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

        @Autowired
        private ProductService productService;

        @Autowired
        private AuthService authService;

        @Autowired
        private ImageService imageService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> create(
                        @RequestHeader("Authorization") String token,
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

                ProductResponse response = productService.create(user, request);

                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Product created successfully")
                                .data(response)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ProductResponse>> search(
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "storeId", required = false) Long storeId,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
                SearchProductRequest request = SearchProductRequest.builder()
                                .name(name)
                                .category(category)
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .storeId(storeId)
                                .page(page)
                                .limit(limit)
                                .build();

                Page<ProductResponse> result = productService.search(request);

                return WebResponse.<List<ProductResponse>>builder()
                                .success(true)
                                .message("List products")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @DeleteMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> delete(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("productId") Long productId) {
                User user = authService.validateToken(token);
                productService.delete(user, productId);
                return WebResponse.<String>builder()
                                .success(true)
                                .message("Product deleted successfully")
                                .build();
        }

        @PutMapping(path = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> update(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("productId") Long productId,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam(value = "price", required = false) BigDecimal price,
                        @RequestParam(value = "stock", required = false) Integer stock,
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "color", required = false) String color,
                        @RequestParam(value = "imageUrl", required = false) MultipartFile imageFile) {
                User user = authService.validateToken(token);

                String imageUrl = null;
                if (imageFile != null && !imageFile.isEmpty()) {
                        imageUrl = imageService.saveImage(imageFile);
                }

                UpdateProductRequest request = UpdateProductRequest.builder()
                                .name(name)
                                .description(description)
                                .price(price)
                                .stock(stock)
                                .category(category)
                                .color(color)
                                .imageUrl(imageUrl)
                                .build();

                ProductResponse response = productService.update(user, productId, request);
                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Product updated successfully")
                                .data(response)
                                .build();
        }

        @GetMapping(path = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> getById(@PathVariable("productId") Long productId) {
                ProductResponse response = productService.get(productId);
                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Successfully get product")
                                .data(response) // Note: Docs said "Product updated successfully" in message for GET,
                                                // but assuming copy-paste error. "Successfully get product" is better.
                                .build();
        }
}