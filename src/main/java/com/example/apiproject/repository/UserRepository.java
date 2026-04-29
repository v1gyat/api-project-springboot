package com.example.apiproject.repository;

import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * Find user by email for authentication
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all active users by role (used by strategy pattern)
     */
    List<User> findAllByRoleAndIsActiveTrue(Role role);

    // Filtered/paged queries are now handled by JpaSpecificationExecutor.findAll(spec, pageable)
    // via UserSpecifications — see UserServiceImpl.getAllUsers()
}
