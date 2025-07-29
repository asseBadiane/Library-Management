package com.library_managemnt.api_gateway;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// public class ApiGatewayApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(ApiGatewayApplication.class, args);
// 	}

// }


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway - Entry point for all client requests
 * Handles routing, load balancing, and cross-cutting concerns
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure routes to microservices
     * Each route includes path predicates and filters for cross-cutting concerns
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .addRequestHeader("X-Correlation-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                                .circuitBreaker(c -> c
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://user-service"))
                
                // Book Service Routes
                .route("book-service", r -> r
                        .path("/api/books/**")
                        .filters(f -> f
                                .addRequestHeader("X-Correlation-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                                .circuitBreaker(c -> c
                                        .setName("book-service-cb")
                                        .setFallbackUri("forward:/fallback/books")))
                        .uri("lb://book-service"))
                
                // Borrow Service Routes
                .route("borrow-service", r -> r
                        .path("/api/borrows/**")
                        .filters(f -> f
                                .addRequestHeader("X-Correlation-ID", "#{T(java.util.UUID).randomUUID().toString()}")
                                .circuitBreaker(c -> c
                                        .setName("borrow-service-cb")
                                        .setFallbackUri("forward:/fallback/borrows")))
                        .uri("lb://borrow-service"))
                
                // Authentication Routes
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                .build();
    }
}