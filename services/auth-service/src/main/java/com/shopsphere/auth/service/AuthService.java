package com.shopsphere.auth.service;

import com.shopsphere.auth.domain.Role;
import com.shopsphere.auth.domain.User;
import com.shopsphere.auth.dto.*;
import com.shopsphere.auth.exception.DuplicateResourceException;
import com.shopsphere.auth.exception.InvalidTokenException;
import com.shopsphere.auth.repository.UserRepository;
import com.shopsphere.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}", savedUser.getUsername());

        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidTokenException("User not found for refresh token"));

        return buildAuthResponse(user);
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return mapToUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
