package com.librar_management.user_service.dto;


/**
 * Data Transfer Object for login responses
 */
public class LoginResponseDto {
    
    private String token;
    private String type = "Bearer";
    private UserResponseDto user;
    
    // Constructors
    public LoginResponseDto() {}
    
    public LoginResponseDto(String token, UserResponseDto user) {
        this.token = token;
        this.user = user;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }
}