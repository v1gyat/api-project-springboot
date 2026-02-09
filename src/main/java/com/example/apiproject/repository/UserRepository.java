package com.example.apiproject.repository;

import com.example.apiproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.example.apiproject.entity.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository provides all basic CRUD operations

    /**
     * Find user by email for authentication
     * 
     * @param email The user's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    List<User> findAllByRoleAndIsActiveTrue(Role role);
}
