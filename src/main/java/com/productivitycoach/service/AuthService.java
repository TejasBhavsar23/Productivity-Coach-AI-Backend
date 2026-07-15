package com.productivitycoach.service;

import com.productivitycoach.dto.request.LoginRequest;
import com.productivitycoach.dto.request.SignupRequest;
import com.productivitycoach.dto.response.AuthResponse;
import com.productivitycoach.entity.Role;
import com.productivitycoach.entity.User;
import com.productivitycoach.exception.DuplicateResourceException;
import com.productivitycoach.repository.UserRepository;
import com.productivitycoach.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and authentication.
 *
 * Signup flow:
 *   1. Check email uniqueness
 *   2. Hash the password with BCrypt
 *   3. Persist the new User
 *   4. Issue a JWT
 *
 * Login flow:
 *   1. Delegate to Spring Security's AuthenticationManager
 *   2. Store the authentication in the SecurityContext
 *   3. Issue a JWT
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    // ----------------------------------------------------------------
    // SIGNUP
    // ----------------------------------------------------------------

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        logger.info("Signup attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account already exists with email: " + request.getEmail());
        }

        User user = new User(
                request.getFullName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );

        User saved = userRepository.save(user);
        logger.info("New user registered: id={}, email={}", saved.getId(), saved.getEmail());

        String token = jwtUtils.generateTokenFromEmail(saved.getEmail());

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole().name()
        );
    }

    // ----------------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------------

    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        // Delegates to DaoAuthenticationProvider → UserDetailsServiceImpl
        // Throws BadCredentialsException automatically on wrong password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        logger.info("Successful login for user id={}", user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }
}
