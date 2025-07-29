package com.library.notificationservice.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.library.notificationservice.dto.UserDto;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);
}
