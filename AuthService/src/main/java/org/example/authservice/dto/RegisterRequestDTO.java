package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;
import org.example.authservice.enums.Role;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "User registration request")
public class RegisterRequestDTO {

    @Schema(description = "User login (email)", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Login is mandatory")
    @Email(message = "Login must be a valid email address")
    private String login;

    @Schema(description = "User password", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is mandatory")
    private String password;

    @Schema(description = "User role", example = "ROLE_USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Role is mandatory")
    private Role role;

    @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is mandatory")
    private String name;

    @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Surname is mandatory")
    private String surname;

    @Schema(description = "User's birth date in YYYY-MM-DD format", example = "1990-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Birth date is mandatory")
    @Past(message = "Birth date must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(description = "Whether the user account is active", example = "true")
    private Boolean active = true;
}