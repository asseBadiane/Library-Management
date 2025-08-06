package com.librar_management.user_service.service;
// user-service/src/main/java/com/library/user/service/UserService.java

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.librar_management.user_service.dto.UserRequestDto;
import com.librar_management.user_service.dto.UserResponseDto;
import com.librar_management.user_service.enums.UserRole;
import com.librar_management.user_service.kafka.UserEventProducer;
import com.librar_management.user_service.model.User;
import com.librar_management.user_service.repository.UserRepository;

import java.util.Optional;

/**
 * User Service - Business logic for user management
 * Handles CRUD operations and publishes events to Kafka
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserEventProducer userEventProducer;
    
    /**
     * Create a new user - ADMIN only operation
     * @throws Exception 
     */
    public UserResponseDto createUser(UserRequestDto userRequest) throws Exception {
        logger.info("Creating new user with username: {}", userRequest.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new Exception("Username already exists: " + userRequest.getUsername());
        }
        
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new Exception("Email already exists: " + userRequest.getEmail());
        }
        
        // Create new user
        User user = new User(
                userRequest.getUsername(),
                passwordEncoder.encode(userRequest.getPassword()),
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getRole()
        );
        
        User savedUser = userRepository.save(user);
        
        // Publish user created event
        userEventProducer.sendUserCreatedEvent(savedUser);
        
        logger.info("User created successfully with ID: {}", savedUser.getId());
        return new UserResponseDto(savedUser);
    }
    
    /**
     * Register a new user (self-registration with READER role)
     * @throws Exception 
     */
    public UserResponseDto registerUser(UserRequestDto userRequest) throws Exception {
        logger.info("Registering new user with username: {}", userRequest.getUsername());
        
        // Force READER role for self-registration
        userRequest.setRole(UserRole.READER);
        
        return createUser(userRequest);
    }
    
    /**
     * Update user role - ADMIN only operation
     */
    public UserResponseDto updateUserRole(Long userId, UserRole newRole) {
        logger.info("Updating role for user ID: {} to role: {}", userId, newRole);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        
        User updatedUser = userRepository.save(user);
        
        // Publish user updated event
        userEventProducer.publishUserUpdated(updatedUser, oldRole);
        
        logger.info("User role updated successfully for user ID: {}", userId);
        return new UserResponseDto(updatedUser);
    }
    
    /**
     * Get user by ID
     */
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        return new UserResponseDto(user);
    }
    
    /**
     * Get user by username (used for authentication)
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get all users with pagination
     */
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable)
                .map(UserResponseDto::new);
    }
    
    /**
     * Search users by name, username, or email
     */
    public Page<UserResponseDto> searchUsers(String search, Pageable pageable) {
        return userRepository.searchActiveUsers(search, pageable)
                .map(UserResponseDto::new);
    }
    
    /**
     * Deactivate user - ADMIN only operation
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        user.setActive(false);
        userRepository.save(user);
        
        // Publish user deactivated event
        userEventProducer.publishUserDeactivated(user);
        
        logger.info("User deactivated successfully: {}", userId);
    }
    
    /**
     * Get user statistics by role
     */
    public long getUserCountByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}
