package com.library.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.library.notificationservice.dto.BookDto;

@FeignClient(name = "book-service")
public interface BookServiceClient {
    
    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable Long id);
}