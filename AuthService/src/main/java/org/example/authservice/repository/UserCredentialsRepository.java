package org.example.authservice.repository;

import org.example.authservice.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {

    // Named methods
    Optional<UserCredentials> findByLoginAndActiveTrue(String login);
    boolean existsByLogin(String login);

}