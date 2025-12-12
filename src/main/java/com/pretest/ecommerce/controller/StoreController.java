package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.*;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import com.pretest.ecommerce.service.ProductService;
import com.pretest.ecommerce.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

        @Autowired
        private StoreService storeService;

        @Autowired
        private ProductService productService;

        @Autowired
        private AuthService authService;

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> create(
                        @RequestHeader("Authorization") String token,
                        @RequestBody CreateStoreRequest request) {
                User user = authService.validateToken(token);
                StoreResponse response = storeService.create(user, request);

                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Store created successfully")
                                .data(response)
                                .build();
        }

        @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> updateStore(@RequestHeader("Authorization") String token,
                        @RequestBody com.pretest.ecommerce.dto.UpdateStoreRequest request) {
                User user = authService.validateToken(token);
                StoreResponse storeResponse = storeService.update(user, request);
                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Store updated successfully")
                                .data(storeResponse)
                                .build();
        }

        @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<StoreResponse>> list(
                        @RequestParam(name = "page", defaultValue = "0") Integer page,
                        @RequestParam(name = "size", defaultValue = "10") Integer size) {
                Page<StoreResponse> result = storeService.findAll(page, size);

                return WebResponse.<List<StoreResponse>>builder()
                                .success(true)
                                .message("Successfully get all stores")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @GetMapping(path = "/{storeId}/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> getProduct(
                        @PathVariable("storeId") Long storeId,
                        @PathVariable("productId") Long productId) {
                ProductResponse response = productService.get(productId);
                if (!response.getStoreId().equals(storeId)) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in this store");
                }
                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Successfully get product")
                                .data(response)
                                .build();
        }

        @GetMapping(path = "/{storeId}/products", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ProductResponse>> listProducts(
                        @PathVariable("storeId") Long storeId,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

                com.pretest.ecommerce.dto.SearchProductRequest request = com.pretest.ecommerce.dto.SearchProductRequest
                                .builder()
                                .storeId(storeId)
                                .page(page)
                                .limit(limit)
                                .build();

                Page<ProductResponse> result = productService.search(request);

                return WebResponse.<List<ProductResponse>>builder()
                                .success(true)
                                .message("Successfully get store products")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @GetMapping(path = "/{storeId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> get(
                        @PathVariable("storeId") Long storeId) {
                StoreResponse storeResponse = storeService.get(storeId);
                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Successfully get store")
                                .data(storeResponse)
                                .build();
        }
}
