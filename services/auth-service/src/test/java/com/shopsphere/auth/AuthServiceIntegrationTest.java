package com.shopsphere.auth;

import com.shopsphere.auth.dto.LoginRequest;
import com.shopsphere.auth.dto.RegisterRequest;
import com.shopsphere.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Auth Service Integration Tests")
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("auth_db")
            .withUsername("shopsphere")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    @DisplayName("Should fail registration with duplicate username")
    void shouldFailOnDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("dupuser");
        request.setEmail("dup@example.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        request.setEmail("different@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Should login with valid credentials")
    void shouldLogin() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginuser");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void shouldFailLoginWithWrongPassword() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpwduser");
        registerRequest.setEmail("wrongpwd@example.com");
        registerRequest.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpwduser");
        loginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return health status")
    void shouldReturnHealth() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Should validate registration input")
    void shouldValidateRegistrationInput() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");  // too short
        request.setEmail("not-an-email");
        request.setPassword("short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
