package com.librar_management.user_service.controller;



import com.librar_management.user_service.dto.UserRequestDto;
import com.librar_management.user_service.dto.UserResponseDto;
import com.librar_management.user_service.enums.UserRole;
import com.librar_management.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for User Management
 * Provides endpoints for CRUD operations on users with role-based access control
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing library users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Create a new user - ADMIN only
     * @throws Exception 
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Create a new user account (Admin only)")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequest) throws Exception {
        UserResponseDto createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by ID")
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Get all users with pagination - ADMIN and LIBRARY_MANAGER only
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARY_MANAGER')")
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponseDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Search users - ADMIN and LIBRARY_MANAGER only
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARY_MANAGER')")
    @Operation(summary = "Search users", description = "Search users by name, username, or email")
    public ResponseEntity<Page<UserResponseDto>> searchUsers(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponseDto> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Update user role - ADMIN only
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Update user role (Admin only)")
    public ResponseEntity<UserResponseDto> updateUserRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New role") @RequestBody Map<String, UserRole> roleRequest) {
        
        UserRole newRole = roleRequest.get("role");
        UserResponseDto updatedUser = userService.updateUserRole(id, newRole);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Deactivate user - ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate user account (Admin only)")
    public ResponseEntity<Map<String, String>> deactivateUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }
    
    /**
     * Get user statistics by role - ADMIN only
     */
    @GetMapping("/stats/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user count by role", description = "Get statistics of users by role")
    public ResponseEntity<Map<String, Long>> getUserCountByRole(
            @Parameter(description = "User role") @PathVariable UserRole role) {
        
        long count = userService.getUserCountByRole(role);
        return ResponseEntity.ok(Map.of("role", count));
    }
}