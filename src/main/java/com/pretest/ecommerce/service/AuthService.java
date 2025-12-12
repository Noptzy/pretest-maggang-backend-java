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

    @Value("${noptzy.jwt.expiration-in-millis}")
    private Long jwtExpiration;

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

        String token = JWT.create()
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole())
                .withClaim("userId", user.getId().toString())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + jwtExpiration))
                .sign(algorithm);

        redisTemplate.opsForValue().set("jwt_token:" + token, user.getId().toString(),
                Duration.ofMillis(jwtExpiration));
        redisTemplate.opsForValue().set("user_online:" + user.getId().toString(), "true",
                Duration.ofMillis(jwtExpiration));

        return TokenResponse.builder()
                .token(token)
                .expiredAt(now + jwtExpiration)
                .build();
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && redisTemplate.hasKey("jwt_token:" + token)) {
            String userId = redisTemplate.opsForValue().get("jwt_token:" + token);
            redisTemplate.delete("jwt_token:" + token);
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