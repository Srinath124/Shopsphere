package com.shopsphere.auth.service;

import com.shopsphere.auth.domain.Role;
import com.shopsphere.auth.domain.User;
import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.RegisterRequest;
import com.shopsphere.auth.exception.DuplicateResourceException;
import com.shopsphere.auth.repository.UserRepository;
import com.shopsphere.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Register should create new user when username and email are unique")
    void registerShouldCreateUserWhenUnique() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("Password@123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$12$hashed");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .email("new@example.com")
                .passwordHash("$2a$12$hashed")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        var response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUser().getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register should throw DuplicateResourceException for duplicate username")
    void registerShouldThrowOnDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setEmail("new@example.com");
        request.setPassword("Password@123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login should return tokens for valid credentials")
    void loginShouldReturnTokens() {
        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 3600000L);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Password@123");

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$12$hashed")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");

        var response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }
}
