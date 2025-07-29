package com.library_managemnt.api_gateway.filter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.library_managemnt.api_gateway.util.JwtUtil;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter
 * Validates JWT tokens and adds user context to requests
 */
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    // Paths that don't require authentication
    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/books/search",
            "/actuator/health"
    );

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip authentication for open endpoints
            if (OPEN_API_ENDPOINTS.stream().anyMatch(path::contains)) {
                return chain.filter(exchange);
            }

            // Extract JWT from Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            
            try {
                // Validate token and extract user info
                if (!jwtUtil.validateToken(token)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String userId = "";
                try {
                    userId = JwtUtil.getUserIdFromToken(token);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String userRole = jwtUtil.getUserRoleFromToken(token);

                // Add user context to request headers
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Role", userRole)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties can be added here
    }
}
