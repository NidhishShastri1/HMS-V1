package com.hms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AllocateBedRequest {
    @NotBlank(message = "Bed number is required")
    private String bedNumber;

    @NotBlank(message = "Ward name is required")
    private String wardName;
}
