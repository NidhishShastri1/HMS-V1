package com.hms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDiagnosisRequest {
    private String diagnosisCode;

    @NotBlank(message = "Diagnosis description is required")
    private String diagnosisDescription;

    @NotNull(message = "isPrimary flag is required")
    private Boolean isPrimary;
}
