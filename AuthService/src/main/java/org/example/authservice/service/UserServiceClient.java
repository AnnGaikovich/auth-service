package org.example.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.authservice.dto.UserServiceRequestDTO;
import org.example.authservice.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Slf4j
@Service
public class UserServiceClient {

    private final WebClient webClient;
    private final String userServiceUrl;

    public UserServiceClient(WebClient webClient,
                             @Value("${app.user-service.url}") String userServiceUrl) {
        this.webClient = webClient;
        this.userServiceUrl = userServiceUrl;
    }

    public Long createUser(UserServiceRequestDTO userRequest) {
        log.info("Creating user in User Service: {}", userRequest.getEmail());

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/v1/internal/users")
                    .bodyValue(userRequest)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> {
                                log.error("Failed to create user in User Service. Status: {}", clientResponse.statusCode());

                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("Error response from User Service: {}", errorBody);
                                            return Mono.error(new BusinessRuleException(
                                                    "Failed to create user in User Service: " + clientResponse.statusCode() + " - " + errorBody
                                            ));
                                        });
                            })
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("id")) {
                return Long.valueOf(response.get("id").toString());
            } else {
                throw new BusinessRuleException("User Service returned invalid response: missing ID");
            }

        } catch (Exception e) {
            log.error("Error calling User Service: {}", e.getMessage());
            throw new BusinessRuleException("User Service is unavailable: " + e.getMessage());
        }
    }

    public void deleteUser(Long userId) {
        log.info("Deleting user in User Service with ID: {}", userId);

        try {
            webClient.delete()
                    .uri("/api/v1/internal/users/" + userId)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse -> {
                                log.error("Failed to delete user in User Service. Status: {}", clientResponse.statusCode());

                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("Error response from User Service: {}", errorBody);
                                            return Mono.error(new BusinessRuleException(
                                                    "Failed to delete user in User Service: " + clientResponse.statusCode() + " - " + errorBody
                                            ));
                                        });
                            })
                    .bodyToMono(Void.class)
                    .block();

            log.info("User successfully deleted from User Service: {}", userId);

        } catch (Exception e) {
            log.error("Error calling User Service to delete user: {}", e.getMessage());
            throw new BusinessRuleException("User Service is unavailable or failed to delete user: " + e.getMessage());
        }
    }
}