package com.pretest.ecommerce.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.pretest.ecommerce.dto.LoginRequest;
import com.pretest.ecommerce.dto.RegisterRequest;
import com.pretest.ecommerce.dto.TokenResponse;
import com.pretest.ecommerce.entity.User;
import com.pretest.ecommerce.repository.UserRepository;
import com.pretest.ecommerce.security.Bcrypt;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${noptzy.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${noptzy.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${noptzy.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public User register(RegisterRequest request) {
        validationService.validate(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(Bcrypt.hashpw(request.getPassword(), Bcrypt.gensalt()));
        user.setName(request.getName());
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    @Transactional
    public User registerSeller(RegisterRequest request) {
        validationService.validate(request);
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(Bcrypt.hashpw(request.getPassword(), Bcrypt.gensalt()));
        user.setName(request.getName());
        user.setRole("SELLER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    public TokenResponse login(LoginRequest request) {
        validationService.validate(request);
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not found"));

        if (!Bcrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or password wrong");
        }

        long now = System.currentTimeMillis();
        Algorithm algorithm = Algorithm.HMAC256(jwtSecretKey);

        String accessToken = JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole())
                .withClaim("userId", user.getId().toString())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + accessTokenExpiration))
                .sign(algorithm);

        String refreshToken = JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole())
                .withClaim("userId", user.getId().toString())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + refreshTokenExpiration))
                .sign(algorithm);

        redisTemplate.opsForValue().set("jwt_token:" + accessToken, user.getId().toString(),
                Duration.ofMillis(accessTokenExpiration));
        redisTemplate.opsForValue().set("jwt_token:" + refreshToken, user.getId().toString(),
                Duration.ofMillis(refreshTokenExpiration));
        redisTemplate.opsForValue().set("user_online:" + user.getId().toString(), "true",
                Duration.ofMillis(refreshTokenExpiration));

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse refreshToken(com.pretest.ecommerce.dto.RefreshTokenRequest request) {
        validationService.validate(request);

        String token = request.getRefreshToken();
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!redisTemplate.hasKey("jwt_token:" + token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or Expired Refresh Token");
        }

        Algorithm algorithm = Algorithm.HMAC256(jwtSecretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        String userId;

        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            userId = decodedJWT.getClaim("userId").asString();
        } catch (JWTVerificationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        redisTemplate.delete("jwt_token:" + token);

        long now = System.currentTimeMillis();

        String newAccessToken = JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole())
                .withClaim("userId", user.getId().toString())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + accessTokenExpiration))
                .sign(algorithm);

        String newRefreshToken = JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole())
                .withClaim("userId", user.getId().toString())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + refreshTokenExpiration))
                .sign(algorithm);

        redisTemplate.opsForValue().set("jwt_token:" + newAccessToken, user.getId().toString(),
                Duration.ofMillis(accessTokenExpiration));
        redisTemplate.opsForValue().set("jwt_token:" + newRefreshToken, user.getId().toString(),
                Duration.ofMillis(refreshTokenExpiration));
        redisTemplate.opsForValue().set("user_online:" + user.getId().toString(), "true",
                Duration.ofMillis(refreshTokenExpiration));

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String redisKey = "jwt_token:" + token;

        if (redisTemplate.hasKey(redisKey)) {
            String userId = redisTemplate.opsForValue().get(redisKey);

            redisTemplate.delete(redisKey);

            if (userId != null) {
                redisTemplate.delete("user_online:" + userId);
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("user_online:" + userId.toString()));
    }

    public User validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String jwtToken = token.substring(7);

        if (!redisTemplate.hasKey("jwt_token:" + jwtToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized or Token Expired");
        }

        Algorithm algorithm = Algorithm.HMAC256(jwtSecretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            DecodedJWT decodedJWT = verifier.verify(jwtToken);
            String userId = decodedJWT.getClaim("userId").asString();

            return userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        } catch (JWTVerificationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    public UUID extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String jwtToken = token.substring(7);
        Algorithm algorithm = Algorithm.HMAC256(jwtSecretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();

        try {
            DecodedJWT decodedJWT = verifier.verify(jwtToken);
            return UUID.fromString(decodedJWT.getClaim("userId").asString());
        } catch (JWTVerificationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }
}