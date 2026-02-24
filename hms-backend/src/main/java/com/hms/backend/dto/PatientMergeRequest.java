package com.hms.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class PatientMergeRequest {
    @NotBlank(message = "Source Patient ID is required")
    private String sourcePatientId;

    @NotBlank(message = "Target Patient ID is required")
    private String targetPatientId;

    public String getSourcePatientId() {
        return sourcePatientId;
    }

    public void setSourcePatientId(String sourcePatientId) {
        this.sourcePatientId = sourcePatientId;
    }

    public String getTargetPatientId() {
        return targetPatientId;
    }

    public void setTargetPatientId(String targetPatientId) {
        this.targetPatientId = targetPatientId;
    }
}
