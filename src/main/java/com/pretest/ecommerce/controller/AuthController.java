package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.dto.RegisterResponse;
import com.pretest.ecommerce.dto.LoginRequest;
import com.pretest.ecommerce.dto.RegisterRequest;
import com.pretest.ecommerce.dto.TokenResponse;
import com.pretest.ecommerce.dto.WebResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
        @Autowired
        private AuthService authService;

        @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
                User user = authService.register(request);
                RegisterResponse registerResponse = RegisterResponse.builder()
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
                return WebResponse.<RegisterResponse>builder()
                                .success(true)
                                .message("User registered successfully")
                                .data(registerResponse)
                                .build();
        }

        @PostMapping(path = "/register/seller", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<RegisterResponse> registerSeller(@RequestBody @Valid RegisterRequest request) {
                User user = authService.registerSeller(request);
                RegisterResponse registerResponse = RegisterResponse.builder()
                                .name(user.getName())
                                .email(user.getEmail())
                                .build();
                return WebResponse.<RegisterResponse>builder()
                                .success(true)
                                .message("Seller registered successfully")
                                .data(registerResponse)
                                .build();
        }

        @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
                TokenResponse tokenResponse = authService.login(request);
                return WebResponse.<TokenResponse>builder()
                                .success(true)
                                .message("User login successfully")
                                .data(tokenResponse)
                                .build();
        }

        @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
        public WebResponse<String> logout(@RequestHeader("Authorization") String token) {
                authService.logout(token);
                return WebResponse.<String>builder()
                                .success(true)
                                .data("Logout Success")
                                .build();
        }
}
