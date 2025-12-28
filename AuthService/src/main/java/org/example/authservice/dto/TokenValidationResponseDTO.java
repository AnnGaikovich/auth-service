package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Token validation response")
public class TokenValidationResponseDTO {

    @Schema(description = "Whether the token is valid", example = "true")
    private boolean valid;

    @Schema(description = "User ID from token", example = "1")
    private Long userId;

    @Schema(description = "User role from token", example = "ROLE_USER")
    private String role;

    @Schema(description = "Username from token", example = "john.doe@example.com")
    private String username;

    @Schema(description = "Error message if token is invalid", example = "Token expired")
    private String message;
}