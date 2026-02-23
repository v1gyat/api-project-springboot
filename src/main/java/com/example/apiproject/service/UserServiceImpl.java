package com.example.apiproject.service;

import com.example.apiproject.dto.*;
import com.example.apiproject.entity.Role;
import com.example.apiproject.entity.User;
import com.example.apiproject.exception.ResourceNotFoundException;
import com.example.apiproject.exception.UnauthorizedException;
import com.example.apiproject.mapper.UserMapper;
import com.example.apiproject.repository.UserRepository;
import com.example.apiproject.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    public UserServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.securityUtils = securityUtils;
    }

    @Override
    public Page<?> getAllUsers(Role role, Boolean isActive, Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();

        if (currentUser.getRole() == Role.MANAGER) {
            // MANAGER: only sees active users with USER role (filters ignored)
            Page<User> users = userRepository.findByRoleAndIsActiveTrue(Role.USER, pageable);
            return users.map(userMapper::toSummaryDTO);
        } else {
            // ADMIN: sees all users with optional filters
            Page<User> users = userRepository.findByFilters(role, isActive, pageable);
            return users.map(userMapper::toAdminDTO);
        }
    }

    @Override
    public UserAdminDTO updateUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        return userMapper.toAdminDTO(updatedUser);
    }

    @Override
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return userMapper.toProfileDTO(user);
    }

    @Override
    @Transactional
    public String updatePassword(String email, PasswordUpdateDTO passwordDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Verify current password
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    @Override
    public UserAdminDTO toggleUserStatus(Long id, Boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setIsActive(isActive);
        User updatedUser = userRepository.save(user);

        return userMapper.toAdminDTO(updatedUser);
    }
}
