package com.library_managemnt.api_gateway.controller;

// public class FallbackController {
    
// }
// package com.library.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Fallback Controller - Handles circuit breaker fallbacks
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "User service is temporarily unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/books")
    public ResponseEntity<Map<String, String>> bookServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Book service is temporarily unavailable",
                        "message", "Please try again later"
                ));
    }

    @GetMapping("/borrows")
    public ResponseEntity<Map<String, String>> borrowServiceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Borrow service is temporarily unavailable",
                        "message", "Please try again later"
                ));
    }
}