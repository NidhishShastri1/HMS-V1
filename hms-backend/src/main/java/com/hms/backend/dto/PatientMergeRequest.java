package com.hms.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientMergeRequest {
    @NotBlank(message = "Source patient ID is mandatory")
    private String sourcePatientId;

    @NotBlank(message = "Target patient ID is mandatory")
    private String targetPatientId;
}
