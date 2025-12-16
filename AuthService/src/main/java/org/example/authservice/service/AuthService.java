package org.example.authservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.*;
import org.example.authservice.entity.UserCredentials;
import org.example.authservice.exception.BusinessRuleException;
import org.example.authservice.exception.UserCredentialsNotFoundException;
import org.example.authservice.repository.UserCredentialsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    public AuthService(UserCredentialsRepository userCredentialsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserServiceClient userServiceClient) {
        this.userCredentialsRepository = userCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userServiceClient = userServiceClient;
    }

    public JwtResponseDTO authenticate(LoginRequestDTO loginRequest) {
        log.info("Attempting authentication for user: {}", loginRequest.getLogin());

        UserCredentials credentials = userCredentialsRepository
                .findByLoginAndActiveTrue(loginRequest.getLogin())
                .orElseThrow(() -> new UserCredentialsNotFoundException(
                        loginRequest.getLogin(), "Invalid login or password")
                );

        if (!passwordEncoder.matches(loginRequest.getPassword(), credentials.getPassword())) {
            log.warn("Invalid password for user: {}", loginRequest.getLogin());
            throw new UserCredentialsNotFoundException(
                    loginRequest.getLogin(), "Invalid login or password"
            );
        }

        log.info("User authenticated successfully: {}", loginRequest.getLogin());

        return generateTokens(credentials);
    }

    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Attempting registration for user: {}", registerRequest.getLogin());

        if (userCredentialsRepository.existsByLogin(registerRequest.getLogin())) {
            throw new BusinessRuleException("Login already exists: " + registerRequest.getLogin());
        }

        UserServiceRequestDTO userRequest = new UserServiceRequestDTO();
        userRequest.setName(registerRequest.getName());
        userRequest.setSurname(registerRequest.getSurname());
        userRequest.setBirthDate(registerRequest.getBirthDate());
        userRequest.setEmail(registerRequest.getLogin());
        userRequest.setActive(registerRequest.getActive());

        Long userId = userServiceClient.createUser(userRequest);

        UserCredentials credentials = new UserCredentials();
        credentials.setLogin(registerRequest.getLogin());
        credentials.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        credentials.setRole(registerRequest.getRole());
        credentials.setUserId(userId);
        credentials.setActive(true);

        UserCredentials savedCredentials = userCredentialsRepository.save(credentials);

        log.info("User registered successfully: {} with ID: {}", registerRequest.getLogin(), userId);

        return generateTokens(savedCredentials);
    }


    public JwtResponseDTO refreshToken(TokenRefreshRequestDTO refreshRequest) {
        log.info("Refreshing token");

        String refreshToken = refreshRequest.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessRuleException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        Long userId = jwtService.extractUserId(refreshToken);

        UserCredentials credentials = userCredentialsRepository
                .findByLoginAndActiveTrue(username)
                .orElseThrow(() -> new UserCredentialsNotFoundException(
                        username, "User not found or inactive")
                );

        if (!credentials.getUserId().equals(userId)) {
            throw new BusinessRuleException("Token user ID mismatch");
        }

        log.info("Token refreshed successfully for user: {}", username);

        return generateTokens(credentials);
    }

    public TokenValidationResponseDTO validateToken(TokenValidationRequestDTO validationRequest) {
        String token = validationRequest.getToken();

        try {
            if (!jwtService.isTokenValid(token)) {
                return new TokenValidationResponseDTO(false, null, null, null, "Invalid token");
            }

            String username = jwtService.extractUsername(token);
            Long userId = jwtService.extractUserId(token);
            String role = jwtService.extractRoles(token).get(0);

            boolean userExists = userCredentialsRepository
                    .findByLoginAndActiveTrue(username)
                    .isPresent();

            if (!userExists) {
                return new TokenValidationResponseDTO(false, null, null, null, "User not found");
            }

            return new TokenValidationResponseDTO(true, userId, role, username, null);

        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new TokenValidationResponseDTO(false, null, null, null, e.getMessage());
        }
    }

    private JwtResponseDTO generateTokens(UserCredentials credentials) {
        String accessToken = jwtService.generateAccessToken(
                credentials.getLogin(),
                credentials.getUserId(),
                credentials.getRole()
        );

        String refreshToken = jwtService.generateRefreshToken(
                credentials.getLogin(),
                credentials.getUserId()
        );

        Long expiresIn = jwtService.getTokenExpiration(accessToken);

        return new JwtResponseDTO(
                accessToken,
                refreshToken,
                expiresIn,
                credentials.getUserId(),
                credentials.getRole().name()
        );

    }

    @Transactional
    public void deleteCredentials(Long userId) {
        log.info("Attempting to delete credentials for userId: {}", userId);

        try {
            log.info("Step 1: Deleting user from UserService for userId: {}", userId);
            userServiceClient.deleteUser(userId);
            log.info("Step 1 completed: User deleted from UserService for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete user from UserService for userId: {}. Error: {}", userId, e.getMessage());
            throw new BusinessRuleException("Failed to delete user from UserService: " + e.getMessage());
        }

        try {
            log.info("Step 2: Deleting credentials from AuthService for userId: {}", userId);
            UserCredentials credentials = userCredentialsRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User credentials not found for userId: " + userId));

            userCredentialsRepository.delete(credentials);
            log.info("Step 2 completed: Credentials deleted from AuthService for userId: {}", userId);

        } catch (EntityNotFoundException e) {
            log.warn("User credentials not found in AuthService for userId: {}, but user was deleted from UserService", userId);
        }

        log.info("User {} successfully deleted from both UserService and AuthService", userId);
    }

}