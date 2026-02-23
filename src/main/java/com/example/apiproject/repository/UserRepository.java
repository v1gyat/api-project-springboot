package com.example.apiproject.repository;

import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email for authentication
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all active users by role (used by strategy pattern)
     */
    List<User> findAllByRoleAndIsActiveTrue(Role role);

    // --- Paged + filtered queries ---

    /**
     * ADMIN view: find users with optional role and isActive filters.
     * Each filter is ignored if null.
     */
    @Query("SELECT u FROM User u WHERE "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findByFilters(
            @Param("role") Role role,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * MANAGER view: find only active users with USER role (paged)
     */
    Page<User> findByRoleAndIsActiveTrue(Role role, Pageable pageable);
}
