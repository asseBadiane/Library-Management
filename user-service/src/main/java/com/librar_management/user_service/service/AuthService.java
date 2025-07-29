package com.librar_management.user_service.service;


import com.librar_management.user_service.dto.LoginRequestDto;
import com.librar_management.user_service.dto.LoginResponseDto;
import com.librar_management.user_service.dto.UserResponseDto;
import com.librar_management.user_service.model.User;
import com.librar_management.user_service.util.JwtUtil;

import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service - Handles user login and JWT token generation
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Authenticate user and generate JWT token
     * @throws InvalidCredentialsException 
     */
    public LoginResponseDto login(LoginRequestDto loginRequest) throws InvalidCredentialsException {
        // logger.info("Authentication attempt for username: {}", loginRequest.getUsername());
        
        User user = userService.getUserByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getUsername()));
        
        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is deactivated");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password attempt for username: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user);
        
        logger.info("User authenticated successfully: {}", user.getUsername());
        return new LoginResponseDto(token, new UserResponseDto(user));
    }
}