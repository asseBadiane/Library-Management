package com.library.borrowservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowApprovalDto {
    @NotNull
    private Long borrowId;
    
    @NotNull
    @JsonAlias({"approvedId", "approvedBy"})
    private Long approverId;
    
    @NotNull
    private Boolean approved;
    
    private String rejectionReason;
}