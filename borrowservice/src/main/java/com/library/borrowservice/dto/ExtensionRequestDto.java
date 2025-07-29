package com.library.borrowservice.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtensionRequestDto {
    @NotNull
    private Long borrowId;
    
    @NotNull
    private LocalDateTime newDueDate;
}