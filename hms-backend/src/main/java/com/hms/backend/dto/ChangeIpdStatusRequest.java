package com.hms.backend.dto;

import com.hms.backend.model.IpdStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeIpdStatusRequest {
    @NotNull(message = "IPD status is required")
    private IpdStatus status;

    private String dischargeSummaryNote;
}
