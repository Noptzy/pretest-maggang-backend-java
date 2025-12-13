package com.pretest.ecommerce.controller;

import com.pretest.ecommerce.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretest.ecommerce.dto.LoginRequest;
import com.pretest.ecommerce.dto.RegisterRequest;
import com.pretest.ecommerce.dto.TokenResponse;
import com.pretest.ecommerce.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AuthService authService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void registerSuccess() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setEmail("test@gmail.com");
                request.setPassword("password");
                request.setName("Test User");

                User user = new User();
                user.setEmail("test@gmail.com");
                user.setName("Test User");

                when(authService.register(any(RegisterRequest.class))).thenReturn(user);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User registered successfully"))
                                .andExpect(jsonPath("$.data.name").value("Test User"))
                                .andExpect(jsonPath("$.data.email").value("test@gmail.com"));

                verify(authService, times(1)).register(any(RegisterRequest.class));
                verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        void registerSellerSuccess() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setEmail("seller@gmail.com");
                request.setPassword("password");
                request.setName("Seller User");

                User user = new User();
                user.setEmail("seller@gmail.com");
                user.setName("Seller User");
                user.setRole("SELLER");

                when(authService.registerSeller(any(RegisterRequest.class))).thenReturn(user);

                mockMvc.perform(post("/api/auth/register/seller")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Seller registered successfully"))
                                .andExpect(jsonPath("$.data.name").value("Seller User"))
                                .andExpect(jsonPath("$.data.email").value("seller@gmail.com"));

                verify(authService, times(1)).registerSeller(any(RegisterRequest.class));
        }

        @Test
        void registerFailed() throws Exception {
                RegisterRequest request = new RegisterRequest();
                request.setEmail("test@gmail.com");
                request.setPassword("password");
                request.setName("Test User");

                doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                                "Email already registered"))
                                .when(authService).register(any(RegisterRequest.class));

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void loginSuccess() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setEmail("test@gmail.com");
                request.setPassword("password");

                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken("test_access_token")
                                .refreshToken("test_refresh_token")
                                .build();

                when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("User login successfully"))
                                .andExpect(jsonPath("$.data.accessToken").value("test_access_token"))
                                .andExpect(jsonPath("$.data.refreshToken").value("test_refresh_token"));
        }

        @Test
        void refreshTokenSuccess() throws Exception {
                com.pretest.ecommerce.dto.RefreshTokenRequest request = new com.pretest.ecommerce.dto.RefreshTokenRequest();
                request.setRefreshToken("Bearer valid_refresh_token");

                TokenResponse tokenResponse = TokenResponse.builder()
                                .accessToken("new_access_token")
                                .refreshToken("new_refresh_token")
                                .build();

                when(authService.refreshToken(any(com.pretest.ecommerce.dto.RefreshTokenRequest.class)))
                                .thenReturn(tokenResponse);

                mockMvc.perform(post("/api/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                                .andExpect(jsonPath("$.data.accessToken").value("new_access_token"))
                                .andExpect(jsonPath("$.data.refreshToken").value("new_refresh_token"));

                verify(authService, times(1)).refreshToken(any(com.pretest.ecommerce.dto.RefreshTokenRequest.class));
        }

        @Test
        void loginFailed() throws Exception {
                LoginRequest request = new LoginRequest();
                request.setEmail("test@gmail.com");
                request.setPassword("wrong_password");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                                                "Email or password wrong"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false)); // Assuming failed responses have
                                                                                // success: false
        }

        @Test
        void logoutSuccess() throws Exception {
                String token = "Bearer test_token";

                mockMvc.perform(post("/api/auth/logout")
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").value("Logout Success"));

                verify(authService, times(1)).logout(token);
        }

        @Test
        void logoutFailed() throws Exception {
                String token = "Bearer invalid_token";

                doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid token"))
                                .when(authService).logout(token);

                mockMvc.perform(post("/api/auth/logout")
                                .header("Authorization", token))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false)); // Assuming failed responses have
                                                                                // success: false
        }
}