package com.library.borrowservice.client;


import com.library.borrowservice.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "book-service")
public interface BookServiceClient {
    
    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable Long id);
    
    // @PutMapping("/api/books/{id}/status")
    // void updateBookStatus(@PathVariable Long id, @RequestParam String status);
    @PutMapping("/api/books/{id}/status")
    void updateBookStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}

