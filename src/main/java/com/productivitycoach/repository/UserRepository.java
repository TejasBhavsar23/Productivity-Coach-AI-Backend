package com.productivitycoach.repository;

import com.productivitycoach.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Provides built-in CRUD via JpaRepository plus custom finders.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (used during login and JWT validation).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether an email already exists in the database (used during signup).
     */
    boolean existsByEmail(String email);
}
