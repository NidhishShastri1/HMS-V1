package com.hms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReopenIpdVisitRequest {
    @NotBlank(message = "Reason is required to reopen IPD visit")
    private String reason;
}
