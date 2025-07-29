package com.library.borrowservice.dto;


import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BorrowRequestDto {
    @NotNull
    private Long bookId;
    
    @NotNull
    private Long userId;
    
    private LocalDateTime requestDate = LocalDateTime.now();
}


