package org.example.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.*;
import org.example.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "API for authentication and credential management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "User authentication", description = "Login to system and receive JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getLogin());
        JwtResponseDTO response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "User registration", description = "Create new user in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getLogin());
        JwtResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Token refresh", description = "Get new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token successfully refreshed",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO refreshRequest) {
        log.info("Token refresh request");
        JwtResponseDTO response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Token validation", description = "Validate JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validation result",
                    content = @Content(schema = @Schema(implementation = TokenValidationResponseDTO.class)))
    })
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponseDTO> validateToken(@Valid @RequestBody TokenValidationRequestDTO validationRequest) {
        log.debug("Token validation request");
        TokenValidationResponseDTO response = authService.validateToken(validationRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete user credentials",
            description = "Delete user credentials. Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Credentials successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @DeleteMapping("/credentials/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // Только ADMIN может удалять учетные данные
    public ResponseEntity<Void> deleteCredentials(
            @Parameter(description = "User ID", required = true, example = "123")
            @PathVariable Long userId) {

        log.info("Delete credentials request for userId: {} by ADMIN", userId);

        try {
            authService.deleteCredentials(userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("User credentials not found for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error deleting credentials for userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}