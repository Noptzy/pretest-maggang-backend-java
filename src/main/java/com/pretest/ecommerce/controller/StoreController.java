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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
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

        @Autowired
        private com.pretest.ecommerce.service.ImageService imageService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> create(
                        @RequestHeader("Authorization") String token,
                        @RequestParam("name") String name,
                        @RequestParam("location") String location,
                        @RequestParam(value = "imageUrl", required = false) MultipartFile imageFile) {
                User user = authService.validateToken(token);

                String imageUrl = null;
                if (imageFile != null && !imageFile.isEmpty()) {
                        imageUrl = imageService.saveImage(imageFile);
                }

                CreateStoreRequest request = new CreateStoreRequest();
                request.setName(name);
                request.setLocation(location);
                request.setImageUrl(imageUrl);

                StoreResponse response = storeService.create(user, request);

                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Store created successfully")
                                .data(response)
                                .build();
        }

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

        @PostMapping(path = { "/my-store", "/my-store-update" }, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> updateStore(@RequestHeader("Authorization") String token,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "location", required = false) String location,
                        @RequestParam(value = "imageUrl", required = false) MultipartFile imageFile) {
                User user = authService.validateToken(token);

                String imageUrl = null;
                if (imageFile != null && !imageFile.isEmpty()) {
                        imageUrl = imageService.saveImage(imageFile);
                }

                com.pretest.ecommerce.dto.UpdateStoreRequest request = com.pretest.ecommerce.dto.UpdateStoreRequest
                                .builder()
                                .name(name)
                                .location(location)
                                .imageUrl(imageUrl)
                                .build();

                StoreResponse storeResponse = storeService.update(user, request);
                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Store updated successfully")
                                .data(storeResponse)
                                .build();
        }

        @PostMapping(path = { "/my-store",
                        "/my-store-update" }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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

        @GetMapping(path = "/my-store", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<StoreResponse> getMyStore(@RequestHeader("Authorization") String token) {
                User user = authService.validateToken(token);
                StoreResponse storeResponse = storeService.findByUser(user);
                return WebResponse.<StoreResponse>builder()
                                .success(true)
                                .message("Successfully get my store")
                                .data(storeResponse)
                                .build();
        }

        @GetMapping(path = "/my-store/products", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ProductResponse>> getMyStoreProducts(
                        @RequestHeader("Authorization") String token,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
                User user = authService.validateToken(token);
                StoreResponse myStore = storeService.findByUser(user);

                com.pretest.ecommerce.dto.SearchProductRequest request = com.pretest.ecommerce.dto.SearchProductRequest
                                .builder()
                                .storeId(myStore.getId())
                                .page(page)
                                .limit(limit)
                                .build();

                Page<ProductResponse> result = productService.search(request);

                return WebResponse.<List<ProductResponse>>builder()
                                .success(true)
                                .message("Successfully get my store products")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<List<ProductResponse>> listAllProducts(
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "storeId", required = false) Long storeId,
                        @RequestParam(value = "page", defaultValue = "0") Integer page,
                        @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
                com.pretest.ecommerce.dto.SearchProductRequest request = com.pretest.ecommerce.dto.SearchProductRequest
                                .builder()
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
                                .message("Successfully get all products")
                                .data(result.getContent())
                                .paging(PagingResponse.builder()
                                                .currentPage(result.getNumber())
                                                .totalPage(result.getTotalPages())
                                                .limit(result.getSize())
                                                .build())
                                .build();
        }

        @PutMapping(path = "/my-store/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> updateProductMyStore(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("productId") Long productId,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam(value = "price", required = false) BigDecimal price,
                        @RequestParam(value = "stock", required = false) Integer stock,
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "color", required = false) String color,
                        @RequestParam(value = "imageUrl", required = false) org.springframework.web.multipart.MultipartFile imageFile) {
                User user = authService.validateToken(token);

                String imageUrl = null;
                if (imageFile != null && !imageFile.isEmpty()) {
                        imageUrl = imageService.saveImage(imageFile);
                }

                com.pretest.ecommerce.dto.UpdateProductRequest request = com.pretest.ecommerce.dto.UpdateProductRequest
                                .builder()
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

        @DeleteMapping(path = "/my-store/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> deleteProductMyStore(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("productId") Long productId) {
                User user = authService.validateToken(token);
                productService.delete(user, productId);
                return WebResponse.<String>builder()
                                .success(true)
                                .message("Product deleted successfully")
                                .build();
        }

        @PostMapping(path = "/my-store/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<ProductResponse> createProductMyStore(
                        @RequestHeader("Authorization") String token,
                        @RequestParam("name") String name,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam("price") BigDecimal price,
                        @RequestParam("stock") Integer stock,
                        @RequestParam("category") String category,
                        @RequestParam(value = "color", required = false) String color,
                        @RequestParam(value = "imageUrl", required = false) MultipartFile imageFile) {
                User user = authService.validateToken(token);

                StoreResponse myStore = storeService.findByUser(user);

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

                ProductResponse response = productService.create(user, myStore.getId(), request);

                return WebResponse.<ProductResponse>builder()
                                .success(true)
                                .message("Product created successfully")
                                .data(response)
                                .build();
        }

        @DeleteMapping(path = "/my-store", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> deleteMyStore(@RequestHeader("Authorization") String token) {
                User user = authService.validateToken(token);
                storeService.delete(user);
                return WebResponse.<String>builder()
                                .success(true)
                                .message("Store deleted successfully")
                                .build();
        }

        @PostMapping(path = "/{storeId}/follow", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> followStore(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("storeId") Long storeId) {
                User user = authService.validateToken(token);
                storeService.followStore(user, storeId);
                return WebResponse.<String>builder()
                                .success(true)
                                .message("Followed store successfully")
                                .build();
        }

        @DeleteMapping(path = "/{storeId}/unfollow", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> unfollowStore(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("storeId") Long storeId) {
                User user = authService.validateToken(token);
                storeService.unfollowStore(user, storeId);
                return WebResponse.<String>builder()
                                .success(true)
                                .message("Unfollowed store successfully")
                                .build();
        }
}
