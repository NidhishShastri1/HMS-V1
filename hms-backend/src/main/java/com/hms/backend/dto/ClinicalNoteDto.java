package com.hms.backend.dto;

import com.hms.backend.model.ClinicalNoteType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClinicalNoteDto {
    private Long id;
    private String visitId;
    private ClinicalNoteType noteType;
    private String noteContent;
    private Integer versionNumber;
    private Long createdByUserId;
    private Instant createdAt;
    private boolean isCurrent;
}
