package org.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Error response with validation details")
public class ErrorResponseDTO {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Validation Failed")
    private String error;

    @Schema(description = "Error message", example = "Validation errors occurred")
    private String message;

    @Schema(description = "Detailed validation errors")
    private Map<String, String> details;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    public ErrorResponseDTO(int status, String error, String message, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }
}