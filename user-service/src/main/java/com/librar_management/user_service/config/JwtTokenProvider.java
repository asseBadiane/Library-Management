package com.librar_management.user_service.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.librar_management.user_service.util.JwtUtil;

@Component
public class JwtTokenProvider {

    @Autowired
    private JwtUtil jwtUtil;

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        return jwtUtil.getAuthentication(token);
    }
}