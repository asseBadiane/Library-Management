package com.librar_management.user_service.controller;

import com.librar_management.user_service.dto.LoginRequestDto;
import com.librar_management.user_service.dto.LoginResponseDto;
import com.librar_management.user_service.dto.UserRequestDto;
import com.librar_management.user_service.dto.UserResponseDto;
import com.librar_management.user_service.service.AuthService;
import com.librar_management.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication
 * Handles user registration and login
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;

    // User registration is a READER role by default
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequest) throws Exception {
        UserResponseDto registeredUser = userService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) throws InvalidCredentialsException {
        LoginResponseDto loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}