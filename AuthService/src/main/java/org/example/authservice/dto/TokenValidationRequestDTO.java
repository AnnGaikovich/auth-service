package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Token validation request")
public class TokenValidationRequestDTO {

    @Schema(description = "JWT token to validate", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Token is mandatory")
    private String token;
}