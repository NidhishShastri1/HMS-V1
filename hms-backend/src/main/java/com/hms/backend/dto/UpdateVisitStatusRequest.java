package com.hms.backend.dto;

import com.hms.backend.model.ClinicalVisitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVisitStatusRequest {
    @NotNull(message = "Visit status is required")
    private ClinicalVisitStatus status;
}
