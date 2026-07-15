package com.productivitycoach.controller;

import com.productivitycoach.dto.request.LoginRequest;
import com.productivitycoach.dto.request.SignupRequest;
import com.productivitycoach.dto.response.AuthResponse;
import com.productivitycoach.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * POST /auth/signup  — Register a new user account
 * POST /auth/login   — Authenticate and receive a JWT token
 *
 * These endpoints are publicly accessible (no JWT required).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user account.
     * Returns a JWT token on success so the user can immediately make authenticated requests.
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new account and returns a JWT token")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        logger.info("POST /auth/signup - email={}", request.getEmail());
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate an existing user.
     * Returns a JWT token to be used in the Authorization header for all subsequent requests.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("POST /auth/login - email={}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
