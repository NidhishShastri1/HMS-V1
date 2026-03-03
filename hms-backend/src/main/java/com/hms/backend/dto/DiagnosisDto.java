package com.hms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DiagnosisDto {
    private Long id;
    private String visitId;
    private String diagnosisCode;
    private String diagnosisDescription;
    private boolean isPrimary;
    private Long createdBy;
    private Instant createdAt;
}
