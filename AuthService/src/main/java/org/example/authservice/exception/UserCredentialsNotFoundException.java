package org.example.authservice.exception;

public class UserCredentialsNotFoundException extends RuntimeException {
    public UserCredentialsNotFoundException(String message) {
        super(message);
    }

    public UserCredentialsNotFoundException(String login, String message) {
        super("User credentials not found with login: " + login + ". " + message);
    }
}